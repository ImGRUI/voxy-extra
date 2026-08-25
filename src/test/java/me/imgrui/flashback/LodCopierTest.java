package me.imgrui.flashback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Copy semantics against a real directory tree: several source bases have to end up as one replay
 * directory without treading on each other, and Voxy's storage config has to come from the base Flashback
 * recorded rather than from whichever world happened to be copied last.
 */
class LodCopierTest {

    /**
     * The copier logs what it decides; these tests assert the decisions it reports instead, and a real
     * logger would only have the test run write a Minecraft log file into the repository.
     */
    private static final Logger LOGGER = NOPLogger.NOP_LOGGER;

    private static final String OVERWORLD = "minecraft_overworld";
    private static final String NETHER = "minecraft_the_nether";
    private static final String LIMBO = "limbo_waiting_room";

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private static String read(Path file) throws IOException {
        return Files.readString(file);
    }

    private static LodCopier.Report copy(LodCopyPlan plan) {
        return LodCopier.copy(plan, LodCopier.JOURNAL_FILTER, LOGGER);
    }

    @Nested
    @DisplayName("gathering sources")
    class GatheringSources {

        @Test
        @DisplayName("gathers three worlds from three different bases into one replay directory")
        void gathersSeveralBases(@TempDir Path root) throws IOException {
            Path address = root.resolve("saves/mc.example.com_25565");
            Path seedA = root.resolve("saves/world-0000000000001234");
            Path seedB = root.resolve("saves/world-0000000000005678");
            Path destination = root.resolve("flashback/replay-uuid");

            write(address.resolve(LIMBO).resolve("sections.db"), "limbo terrain");
            write(seedA.resolve(OVERWORLD).resolve("sections.db"), "overworld terrain");
            write(seedB.resolve(NETHER).resolve("sections.db"), "nether terrain");
            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{\"address\":true}");
            // The seed directories carry a config of their own in this fixture precisely so the copy can
            // be caught taking the wrong one.
            write(seedA.resolve(LodCopyPlan.CONFIG_FILE), "{\"seedA\":true}");
            write(seedB.resolve(LodCopyPlan.CONFIG_FILE), "{\"seedB\":true}");

            LodCopier.Report report = copy(LodCopyPlan.of(Set.of(LIMBO, OVERWORLD, NETHER),
                    Map.of(LIMBO, new WorldLodBase(address, false),
                            OVERWORLD, new WorldLodBase(seedA, true),
                            NETHER, new WorldLodBase(seedB, true)),
                    address, destination));

            assertEquals(List.of(LIMBO, OVERWORLD, NETHER), report.copied());
            assertEquals(List.of(), report.missing());
            assertFalse(report.copiedNothing());
            assertEquals("limbo terrain", read(destination.resolve(LIMBO).resolve("sections.db")));
            assertEquals("overworld terrain", read(destination.resolve(OVERWORLD).resolve("sections.db")));
            assertEquals("nether terrain", read(destination.resolve(NETHER).resolve("sections.db")));
        }

        @Test
        @DisplayName("keeps two worlds of the same name apart because a replay only ever sees one of them")
        void worldIdsAreUniquePerReplay(@TempDir Path root) throws IOException {
            // Two bases both holding a directory called minecraft_overworld: the plan is keyed by world id,
            // so only the base that world was observed at is read and the other is left alone.
            Path address = root.resolve("saves/mc.example.com_25565");
            Path seed = root.resolve("saves/world-0000000000001234");
            Path destination = root.resolve("flashback/replay-uuid");

            write(address.resolve(OVERWORLD).resolve("sections.db"), "stale address copy");
            write(seed.resolve(OVERWORLD).resolve("sections.db"), "live seed copy");
            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{}");

            copy(LodCopyPlan.of(Set.of(OVERWORLD), Map.of(OVERWORLD, new WorldLodBase(seed, true)),
                    address, destination));

            assertEquals("live seed copy", read(destination.resolve(OVERWORLD).resolve("sections.db")));
            assertEquals(1, Files.list(destination).filter(Files::isDirectory).count());
        }

        @Test
        @DisplayName("leaves out voxy's live database journal")
        void filtersTheJournal(@TempDir Path root) throws IOException {
            Path address = root.resolve("saves/mc.example.com_25565");
            Path destination = root.resolve("flashback/replay-uuid");

            write(address.resolve(OVERWORLD).resolve("sections.db"), "terrain");
            write(address.resolve(OVERWORLD).resolve("LOG"), "log");
            write(address.resolve(OVERWORLD).resolve("LOCK"), "lock");
            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{}");

            copy(LodCopyPlan.of(Set.of(OVERWORLD), Map.of(OVERWORLD, new WorldLodBase(address, false)),
                    address, destination));

            assertTrue(Files.exists(destination.resolve(OVERWORLD).resolve("sections.db")));
            assertFalse(Files.exists(destination.resolve(OVERWORLD).resolve("LOG")));
            assertFalse(Files.exists(destination.resolve(OVERWORLD).resolve("LOCK")));
        }
    }

    @Nested
    @DisplayName("storage config")
    class StorageConfig {

        @Test
        @DisplayName("takes config.json from the recorded base, not from a seed directory")
        void configComesFromTheRecordedBase(@TempDir Path root) throws IOException {
            Path address = root.resolve("saves/mc.example.com_25565");
            Path seed = root.resolve("saves/world-0000000000001234");
            Path destination = root.resolve("flashback/replay-uuid");

            write(seed.resolve(OVERWORLD).resolve("sections.db"), "overworld terrain");
            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{\"address\":true}");
            write(seed.resolve(LodCopyPlan.CONFIG_FILE), "{\"seed\":true}");

            copy(LodCopyPlan.of(Set.of(OVERWORLD), Map.of(OVERWORLD, new WorldLodBase(seed, true)),
                    address, destination));

            assertEquals("{\"address\":true}", read(destination.resolve(LodCopyPlan.CONFIG_FILE)));
        }
    }

    @Nested
    @DisplayName("reporting")
    class Reporting {

        @Test
        @DisplayName("reports a world whose source does not exist and copies the rest anyway")
        void reportsMissingSources(@TempDir Path root) throws IOException {
            Path address = root.resolve("saves/mc.example.com_25565");
            Path seed = root.resolve("saves/world-0000000000001234");
            Path destination = root.resolve("flashback/replay-uuid");

            write(address.resolve(LIMBO).resolve("sections.db"), "limbo terrain");
            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{}");
            // Nothing was ever written for the overworld under its seed base.

            LodCopier.Report report = copy(LodCopyPlan.of(Set.of(LIMBO, OVERWORLD),
                    Map.of(LIMBO, new WorldLodBase(address, false), OVERWORLD, new WorldLodBase(seed, true)),
                    address, destination));

            assertEquals(List.of(OVERWORLD), report.missing());
            assertEquals(List.of(LIMBO), report.copied());
            assertFalse(report.copiedNothing());
            assertFalse(Files.exists(destination.resolve(OVERWORLD)));
            assertTrue(Files.exists(destination.resolve(LIMBO).resolve("sections.db")));
            assertTrue(Files.exists(destination.resolve(LodCopyPlan.CONFIG_FILE)));
        }

        @Test
        @DisplayName("says it copied nothing when no world had anything stored")
        void everyWorldMissing(@TempDir Path root) throws IOException {
            Path address = root.resolve("saves/mc.example.com_25565");
            Path seed = root.resolve("saves/world-0000000000001234");
            Path destination = root.resolve("flashback/replay-uuid");

            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{}");

            LodCopier.Report report = copy(LodCopyPlan.of(Set.of(LIMBO, OVERWORLD),
                    Map.of(LIMBO, new WorldLodBase(address, false), OVERWORLD, new WorldLodBase(seed, true)),
                    address, destination));

            assertTrue(report.copiedNothing());
            assertEquals(List.of(), report.copied());
            assertEquals(List.of(LIMBO, OVERWORLD), report.missing());
        }

        @Test
        @DisplayName("copies nothing but the config for a recording that visited no world")
        void emptyPlan(@TempDir Path root) throws IOException {
            Path address = root.resolve("saves/mc.example.com_25565");
            Path destination = root.resolve("flashback/replay-uuid");
            write(address.resolve(LodCopyPlan.CONFIG_FILE), "{}");

            LodCopier.Report report = copy(LodCopyPlan.of(Set.of(), Map.of(), address, destination));

            assertEquals(List.of(), report.copied());
            assertEquals(List.of(), report.missing());
            assertTrue(report.copiedNothing());
            assertEquals(List.of(LodCopyPlan.CONFIG_FILE),
                    Files.list(destination).map(path -> path.getFileName().toString()).toList());
        }
    }
}
