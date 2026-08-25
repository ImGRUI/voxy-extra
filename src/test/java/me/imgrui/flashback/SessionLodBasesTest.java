package me.imgrui.flashback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure decision behind the recorded storage path: does one directory stand for this
 * session's LoDs, and if the replay ends up recording something else, is that worth saying out loud?
 * <p>
 * Nothing here touches Minecraft, Voxy or the file system.
 */
class SessionLodBasesTest {

    /** What Voxy records by itself: the base named after the address the player connected to. */
    private static final Path ADDRESS = Path.of("/game/.voxy/saves/mc.example.com_25565");

    private static final Path SEED_A = Path.of("/game/.voxy/saves/world-0000000000001234");
    private static final Path SEED_B = Path.of("/game/.voxy/saves/world-0000000000005678");

    private static final String OVERWORLD = "minecraft_overworld";
    private static final String NETHER = "minecraft_the_nether";
    private static final String END = "minecraft_the_end";
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

        SessionLodBases session() {
            return SessionLodBases.of(this.bases);
        }
    }

    @Nested
    @DisplayName("soleSeedBase")
    class SoleSeedBase {

        @Test
        @DisplayName("is the one directory a single seed session used")
        void singleSeedSingleWorld() {
            SessionLodBases session = observed().seedKeyed(OVERWORLD, SEED_A).session();

            assertEquals(Optional.of(SEED_A), session.soleSeedBase());
            assertTrue(session.seedKeyingApplied());
        }

        @Test
        @DisplayName("is still that one directory when several worlds share it")
        void singleSeedManyWorlds() {
            SessionLodBases session = observed()
                    .seedKeyed(OVERWORLD, SEED_A)
                    .seedKeyed(NETHER, SEED_A)
                    .seedKeyed(END, SEED_A)
                    .session();

            assertEquals(Optional.of(SEED_A), session.soleSeedBase());
            assertEquals(List.of(SEED_A), session.seedBases());
        }

        @Test
        @DisplayName("gives up when the session crossed worlds with different seeds")
        void multipleSeeds() {
            SessionLodBases session = observed()
                    .seedKeyed(OVERWORLD, SEED_A)
                    .seedKeyed(NETHER, SEED_B)
                    .session();

            assertEquals(Optional.empty(), session.soleSeedBase());
            assertTrue(session.seedKeyingApplied());
            assertEquals(2, session.seedBases().size());
        }

        @Test
        @DisplayName("does not count a waiting room that stayed under the address directory")
        void placeholderSeedIsNotASecondSeed() {
            // The limbo sends the placeholder seed, so seed keying never applied to it and it must not
            // make a single seed session look like a multi seed one.
            SessionLodBases session = observed()
                    .addressKeyed(LIMBO, ADDRESS)
                    .seedKeyed(OVERWORLD, SEED_A)
                    .session();

            assertEquals(Optional.of(SEED_A), session.soleSeedBase());
        }

        @Test
        @DisplayName("gives up on a waiting room plus two real seeds all the same")
        void placeholderPlusMultipleSeeds() {
            SessionLodBases session = observed()
                    .addressKeyed(LIMBO, ADDRESS)
                    .seedKeyed(OVERWORLD, SEED_A)
                    .seedKeyed(NETHER, SEED_B)
                    .session();

            assertEquals(Optional.empty(), session.soleSeedBase());
        }

        @Test
        @DisplayName("has nothing to offer when seed keying moved no world at all")
        void seedKeyingOff() {
            SessionLodBases session = observed()
                    .addressKeyed(OVERWORLD, ADDRESS)
                    .addressKeyed(NETHER, ADDRESS)
                    .session();

            assertEquals(Optional.empty(), session.soleSeedBase());
            assertFalse(session.seedKeyingApplied());
        }

        @Test
        @DisplayName("has nothing to offer for a session nothing was observed of")
        void emptySession() {
            SessionLodBases session = SessionLodBases.of(Map.of());

            assertEquals(Optional.empty(), session.soleSeedBase());
            assertFalse(session.seedKeyingApplied());
            assertEquals(List.of(), session.seedBases());
        }

        @Test
        @DisplayName("lists the bases in a stable order however the worlds were observed")
        void stableOrder() {
            List<Path> forwards = observed().seedKeyed(OVERWORLD, SEED_A).seedKeyed(NETHER, SEED_B)
                    .session().seedBases();
            List<Path> backwards = observed().seedKeyed(NETHER, SEED_B).seedKeyed(OVERWORLD, SEED_A)
                    .session().seedBases();

            assertEquals(forwards, backwards);
            assertEquals(List.of(SEED_A, SEED_B), forwards);
        }
    }

    @Nested
    @DisplayName("pointsOutsideSeedStorage")
    class OutsideSeedStorage {

        @Test
        @DisplayName("stays quiet once the replay records the session's own seed directory")
        void recordedPathIsTheSeedBase() {
            SessionLodBases session = observed().seedKeyed(OVERWORLD, SEED_A).session();

            assertFalse(session.pointsOutsideSeedStorage(SEED_A));
        }

        @Test
        @DisplayName("fires when the replay is left pointing at the address directory")
        void recordedPathIsTheAddressDirectory() {
            // What a multi seed session falls back to: voxy's own value, which finds nothing on playback.
            SessionLodBases session = observed()
                    .seedKeyed(OVERWORLD, SEED_A)
                    .seedKeyed(NETHER, SEED_B)
                    .session();

            assertTrue(session.pointsOutsideSeedStorage(ADDRESS));
        }

        @Test
        @DisplayName("fires when the replay records no path at all")
        void noRecordedPath() {
            SessionLodBases session = observed().seedKeyed(OVERWORLD, SEED_A).session();

            assertTrue(session.pointsOutsideSeedStorage(null));
        }

        @Test
        @DisplayName("stays quiet when seed keying moved nothing, whatever the replay records")
        void seedKeyingOffIsNeverWrong() {
            SessionLodBases session = observed().addressKeyed(OVERWORLD, ADDRESS).session();

            assertFalse(session.pointsOutsideSeedStorage(ADDRESS));
            assertFalse(session.pointsOutsideSeedStorage(null));
            assertFalse(SessionLodBases.of(Map.of()).pointsOutsideSeedStorage(ADDRESS));
        }

        @Test
        @DisplayName("stays quiet on a successful rewrite even when voxy's base path is relative")
        void relativeBaseIsNotAMismatch() {
            // Voxy normalises its base path but never makes it absolute, while what a replay records is an
            // absolute path. Held as they come, the successful case would read as a mismatch and warn on
            // every recording made from a game directory given as a relative path.
            Path relative = Path.of(".voxy/saves/world-0000000000001234");
            SessionLodBases session = observed().seedKeyed(OVERWORLD, relative).session();

            Path recorded = session.soleSeedBase().orElseThrow();

            assertTrue(recorded.isAbsolute(), "what gets written into the replay must be absolute");
            assertFalse(session.pointsOutsideSeedStorage(recorded));
            assertFalse(session.pointsOutsideSeedStorage(relative));
        }

        @Test
        @DisplayName("accepts any of the bases a multi seed session used")
        void anySeedBaseCounts() {
            SessionLodBases session = observed()
                    .seedKeyed(OVERWORLD, SEED_A)
                    .seedKeyed(NETHER, SEED_B)
                    .session();

            assertFalse(session.pointsOutsideSeedStorage(SEED_B));
        }
    }
}
