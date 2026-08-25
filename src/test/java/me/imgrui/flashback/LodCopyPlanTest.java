package me.imgrui.flashback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure decision behind copying a replay's LoDs: given what was observed about where the
 * session's worlds actually stored their terrain, which directory does each world get copied from?
 * <p>
 * Nothing here touches Minecraft, Voxy or the file system.
 */
class LodCopyPlanTest {

    /** What Flashback records: the base Voxy's client instance was built with, named after the address. */
    private static final Path ADDRESS = Path.of("/game/.voxy/saves/mc.example.com_25565");

    private static final Path SEED_A = Path.of("/game/.voxy/saves/world-0000000000001234");
    private static final Path SEED_B = Path.of("/game/.voxy/saves/world-0000000000005678");

    private static final Path DESTINATION =
            Path.of("/game/.voxy/flashback/0f5c8a1e-3b2d-4a77-9c10-8de4b6f0a921");

    private static final String OVERWORLD = "minecraft_overworld";
    private static final String NETHER = "minecraft_the_nether";
    private static final String LIMBO = "limbo_waiting_room";

    /** What the storage mixin would have observed of a session, world by world. */
    private static ObservedBases observed() {
        return new ObservedBases();
    }

    private static final class ObservedBases {
        private final Map<String, WorldLodBase> bases = new LinkedHashMap<>();

        /** A world seed keying applied to, stored under its seed directory. */
        ObservedBases seedKeyed(String worldId, Path base) {
            this.bases.put(worldId, new WorldLodBase(base, true));
            return this;
        }

        /** A world seed keying did not apply to, left under the address directory. */
        ObservedBases addressKeyed(String worldId, Path base) {
            this.bases.put(worldId, new WorldLodBase(base, false));
            return this;
        }

        Map<String, WorldLodBase> map() {
            return this.bases;
        }
    }

    private static LodCopyPlan plan(Set<String> worldIds, Map<String, WorldLodBase> bases) {
        return LodCopyPlan.of(worldIds, bases, ADDRESS, DESTINATION);
    }

    @Nested
    @DisplayName("sources")
    class Sources {

        @Test
        @DisplayName("takes every world of a single seed session from that session's seed directory")
        void singleSeed() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD, NETHER),
                    observed().seedKeyed(OVERWORLD, SEED_A).seedKeyed(NETHER, SEED_A).map());

            assertEquals(List.of(SEED_A.resolve(OVERWORLD), SEED_A.resolve(NETHER)),
                    plan.entries().stream().map(LodCopyPlan.Entry::source).toList());
        }

        @Test
        @DisplayName("takes each world of a multi seed session from its own seed directory")
        void multipleSeeds() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD, NETHER),
                    observed().seedKeyed(OVERWORLD, SEED_A).seedKeyed(NETHER, SEED_B).map());

            assertEquals(Map.of(NETHER, SEED_B.resolve(NETHER), OVERWORLD, SEED_A.resolve(OVERWORLD)),
                    plan.entries().stream().collect(
                            Collectors.toMap(LodCopyPlan.Entry::worldId, LodCopyPlan.Entry::source)));
        }

        @Test
        @DisplayName("mixes seed keyed and address keyed worlds of one session without confusing them")
        void mixedSession() {
            // A limbo waiting room sends the placeholder seed, so it stays under the address directory
            // while the real world it hands the player over to is seed keyed.
            LodCopyPlan plan = plan(Set.of(LIMBO, OVERWORLD),
                    observed().addressKeyed(LIMBO, ADDRESS).seedKeyed(OVERWORLD, SEED_A).map());

            assertEquals(List.of(
                            new LodCopyPlan.Entry(LIMBO, ADDRESS.resolve(LIMBO), DESTINATION.resolve(LIMBO)),
                            new LodCopyPlan.Entry(OVERWORLD, SEED_A.resolve(OVERWORLD), DESTINATION.resolve(OVERWORLD))),
                    plan.entries());
        }

        @Test
        @DisplayName("falls back to the recorded base for a world nothing was observed about")
        void unobservedWorld() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD), Map.of());

            assertEquals(List.of(ADDRESS.resolve(OVERWORLD)),
                    plan.entries().stream().map(LodCopyPlan.Entry::source).toList());
        }

        @Test
        @DisplayName("is exactly the old single base copy when nothing moved any world")
        void nothingMoved() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD, NETHER),
                    observed().addressKeyed(OVERWORLD, ADDRESS).addressKeyed(NETHER, ADDRESS).map());

            assertEquals(List.of(ADDRESS.resolve(OVERWORLD), ADDRESS.resolve(NETHER)),
                    plan.entries().stream().map(LodCopyPlan.Entry::source).toList());
            assertFalse(plan.voxyLooksSeedAware());
        }

        @Test
        @DisplayName("plans no world at all for a recording that visited none")
        void emptySession() {
            LodCopyPlan plan = plan(Set.of(), observed().seedKeyed(OVERWORLD, SEED_A).map());

            assertEquals(List.of(), plan.entries());
            assertEquals(ADDRESS.resolve(LodCopyPlan.CONFIG_FILE), plan.configSource());
        }

        @Test
        @DisplayName("orders worlds the same way twice so two plans can be compared")
        void deterministicOrder() {
            List<String> first = plan(new LinkedHashSet<>(List.of(NETHER, OVERWORLD, LIMBO)), Map.of())
                    .entries().stream().map(LodCopyPlan.Entry::worldId).toList();
            List<String> second = plan(new LinkedHashSet<>(List.of(LIMBO, OVERWORLD, NETHER)), Map.of())
                    .entries().stream().map(LodCopyPlan.Entry::worldId).toList();

            assertEquals(first, second);
            assertEquals(List.of(LIMBO, OVERWORLD, NETHER), first);
        }
    }

    @Nested
    @DisplayName("destinations")
    class Destinations {

        @Test
        @DisplayName("gathers worlds from several bases into the one replay directory without colliding")
        void oneDirectoryPerReplay() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD, NETHER, LIMBO),
                    observed().seedKeyed(OVERWORLD, SEED_A).seedKeyed(NETHER, SEED_B).addressKeyed(LIMBO, ADDRESS).map());

            List<Path> destinations = plan.entries().stream().map(LodCopyPlan.Entry::destination).toList();
            assertEquals(3, destinations.size());
            assertEquals(3, Set.copyOf(destinations).size());
            assertTrue(destinations.stream().allMatch(path -> path.getParent().equals(DESTINATION)));
        }
    }

    @Nested
    @DisplayName("config.json")
    class Config {

        @Test
        @DisplayName("comes from the recorded base even when every world was moved elsewhere")
        void alwaysFromTheRecordedBase() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD, NETHER),
                    observed().seedKeyed(OVERWORLD, SEED_A).seedKeyed(NETHER, SEED_B).map());

            assertEquals(ADDRESS.resolve(LodCopyPlan.CONFIG_FILE), plan.configSource());
            assertEquals(DESTINATION.resolve(LodCopyPlan.CONFIG_FILE), plan.configDestination());
        }
    }

    @Nested
    @DisplayName("voxyLooksSeedAware")
    class SeedAwareSentinel {

        @Test
        @DisplayName("stays quiet while voxy still reports the address base for a world this mod moved")
        void quietWhileVoxyIsAddressKeyed() {
            LodCopyPlan plan = plan(Set.of(OVERWORLD), observed().seedKeyed(OVERWORLD, SEED_A).map());

            assertFalse(plan.voxyLooksSeedAware());
            assertEquals(List.of(), plan.seedAwareWorlds());
        }

        @Test
        @DisplayName("fires when voxy reports the very base seed keying produced")
        void firesWhenVoxyReportsTheSeedBase() {
            // Voxy having become seed aware is exactly this: the path it recorded is the seed keyed one.
            LodCopyPlan plan = LodCopyPlan.of(Set.of(OVERWORLD),
                    observed().seedKeyed(OVERWORLD, SEED_A).map(), SEED_A, DESTINATION);

            assertTrue(plan.voxyLooksSeedAware());
            assertEquals(List.of(OVERWORLD), plan.seedAwareWorlds());
        }

        @Test
        @DisplayName("stays quiet for a world seed keying never applied to, however the bases line up")
        void quietWhenSeedKeyingDidNotApply() {
            LodCopyPlan plan = plan(Set.of(LIMBO), observed().addressKeyed(LIMBO, ADDRESS).map());

            assertFalse(plan.voxyLooksSeedAware());
        }

        @Test
        @DisplayName("names only the worlds that agree, not the whole session")
        void namesOnlyTheAgreeingWorlds() {
            LodCopyPlan plan = LodCopyPlan.of(Set.of(OVERWORLD, NETHER),
                    observed().seedKeyed(OVERWORLD, SEED_A).seedKeyed(NETHER, SEED_B).map(), SEED_A, DESTINATION);

            assertEquals(List.of(OVERWORLD), plan.seedAwareWorlds());
        }
    }
}
