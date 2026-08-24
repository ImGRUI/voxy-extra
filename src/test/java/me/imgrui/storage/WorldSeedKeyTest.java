package me.imgrui.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure key-selection logic behind the {@code worldSeedStorage} option.
 * <p>
 * Nothing here touches Minecraft or Voxy: every input is a primitive that the mixin observes
 * from live state and hands over.
 */
class WorldSeedKeyTest {

    /** {@code .voxy/saves/<address>} - what voxy's {@code getBasePath()} produces for a remote server. */
    private static final Path ADDRESS_PATH = Path.of("/game/.voxy/saves/mc.example.com_25566");

    private static final long SEED = 0xffL;

    /** A plain remote multiplayer session with the option turned on. */
    private static LodStorageContext remote() {
        return new LodStorageContext(true, false, true, false, false, false);
    }

    @Nested
    @DisplayName("directoryName")
    class DirectoryName {

        @Test
        @DisplayName("is world- plus a zero padded 16 digit lowercase hex seed")
        void formatsPositiveSeed() {
            assertEquals("world-00000000000000ff", WorldSeedKey.directoryName(0xffL));
        }

        @Test
        @DisplayName("renders a negative seed as unsigned two's complement")
        void formatsNegativeSeed() {
            assertEquals("world-ffffffffffffffff", WorldSeedKey.directoryName(-1L));
            assertEquals("world-8000000000000000", WorldSeedKey.directoryName(Long.MIN_VALUE));
        }

        @Test
        @DisplayName("never truncates or pads a full width seed")
        void formatsFullWidthSeed() {
            assertEquals("world-123456789abcdef0", WorldSeedKey.directoryName(0x123456789abcdef0L));
            assertEquals(22, WorldSeedKey.directoryName(0L).length());
        }
    }

    @Nested
    @DisplayName("appliesTo")
    class AppliesTo {

        @Test
        @DisplayName("holds for a remote multiplayer session with the option on and a real seed")
        void remoteMultiplayer() {
            assertTrue(WorldSeedKey.appliesTo(remote(), SEED));
        }

        @Test
        @DisplayName("does not hold when the option is off")
        void optionOff() {
            LodStorageContext off = new LodStorageContext(false, false, true, false, false, false);
            assertFalse(WorldSeedKey.appliesTo(off, SEED));
        }

        @Test
        @DisplayName("does not hold in singleplayer")
        void singleplayer() {
            LodStorageContext singleplayer = new LodStorageContext(true, true, false, false, false, false);
            assertFalse(WorldSeedKey.appliesTo(singleplayer, SEED));
        }

        @Test
        @DisplayName("does not hold when the server data voxy keys on is missing")
        void noServerData() {
            LodStorageContext unknown = new LodStorageContext(true, false, false, false, false, false);
            assertFalse(WorldSeedKey.appliesTo(unknown, SEED));
        }

        @Test
        @DisplayName("does not hold on Realms")
        void realms() {
            LodStorageContext realms = new LodStorageContext(true, false, true, true, false, false);
            assertFalse(WorldSeedKey.appliesTo(realms, SEED));
        }

        @Test
        @DisplayName("does not hold while a Flashback replay is being played back")
        void flashbackReplay() {
            LodStorageContext flashback = new LodStorageContext(true, false, true, false, true, false);
            assertFalse(WorldSeedKey.appliesTo(flashback, SEED));
        }

        @Test
        @DisplayName("does not hold while a ReplayMod replay is being played back")
        void replayModReplay() {
            LodStorageContext replayMod = new LodStorageContext(true, false, true, false, false, true);
            assertFalse(WorldSeedKey.appliesTo(replayMod, SEED));
        }

        @Test
        @DisplayName("does not hold for the seed 0 placeholder that limbo servers send")
        void placeholderSeed() {
            assertFalse(WorldSeedKey.appliesTo(remote(), 0L));
        }

        @Test
        @DisplayName("holds for a negative seed")
        void negativeSeed() {
            assertTrue(WorldSeedKey.appliesTo(remote(), -1L));
        }
    }

    @Nested
    @DisplayName("redirect")
    class Redirect {

        @Test
        @DisplayName("swaps the address segment for the seed key and keeps the rest of the path")
        void swapsAddressSegment() {
            Path redirected = WorldSeedKey.redirect(ADDRESS_PATH, remote(), SEED);
            assertEquals(Path.of("/game/.voxy/saves/world-00000000000000ff"), redirected);
        }

        @Test
        @DisplayName("returns the very same path instance when the option is off")
        void optionOffIsVerbatim() {
            LodStorageContext off = new LodStorageContext(false, false, true, false, false, false);
            assertSame(ADDRESS_PATH, WorldSeedKey.redirect(ADDRESS_PATH, off, SEED));
        }

        @Test
        @DisplayName("returns the very same path instance for every non applicable context")
        void nonApplicableIsVerbatim() {
            assertSame(ADDRESS_PATH, WorldSeedKey.redirect(ADDRESS_PATH,
                    new LodStorageContext(true, true, false, false, false, false), SEED));
            assertSame(ADDRESS_PATH, WorldSeedKey.redirect(ADDRESS_PATH,
                    new LodStorageContext(true, false, true, true, false, false), SEED));
            assertSame(ADDRESS_PATH, WorldSeedKey.redirect(ADDRESS_PATH,
                    new LodStorageContext(true, false, true, false, true, false), SEED));
            assertSame(ADDRESS_PATH, WorldSeedKey.redirect(ADDRESS_PATH,
                    new LodStorageContext(true, false, true, false, false, true), SEED));
            assertSame(ADDRESS_PATH, WorldSeedKey.redirect(ADDRESS_PATH, remote(), 0L));
        }

        @Test
        @DisplayName("keys a Flashback style replay base path verbatim as well")
        void replayBasePathIsVerbatim() {
            Path replayPath = Path.of("/game/.flashback/replay_lods/some_replay");
            LodStorageContext flashback = new LodStorageContext(true, false, true, false, true, false);
            assertSame(replayPath, WorldSeedKey.redirect(replayPath, flashback, SEED));
        }

        @Test
        @DisplayName("gives two worlds of one session two different directories")
        void limboAndOverworldGetTheirOwnDirectory() {
            // The waiting room sends the seed 0 placeholder, the real server sends a real seed.
            // Voxy builds one world engine per WorldIdentifier, so both go through redirect separately.
            Path limbo = WorldSeedKey.redirect(ADDRESS_PATH, remote(), 0L);
            Path overworld = WorldSeedKey.redirect(ADDRESS_PATH, remote(), 0x1234L);

            assertSame(ADDRESS_PATH, limbo);
            assertEquals(Path.of("/game/.voxy/saves/world-0000000000001234"), overworld);
        }

        @Test
        @DisplayName("gives two real worlds their own directory")
        void twoRealWorldsGetTheirOwnDirectory() {
            Path first = WorldSeedKey.redirect(ADDRESS_PATH, remote(), 0x1234L);
            Path second = WorldSeedKey.redirect(ADDRESS_PATH, remote(), 0x5678L);

            assertEquals(Path.of("/game/.voxy/saves/world-0000000000001234"), first);
            assertEquals(Path.of("/game/.voxy/saves/world-0000000000005678"), second);
        }
    }

    @Nested
    @DisplayName("LodStorageContext")
    class Context {

        @Test
        @DisplayName("calls only a non local, non realms session with server data remote multiplayer")
        void remoteMultiplayerDerivation() {
            assertTrue(new LodStorageContext(true, false, true, false, false, false).remoteMultiplayer());
            assertFalse(new LodStorageContext(true, true, true, false, false, false).remoteMultiplayer());
            assertFalse(new LodStorageContext(true, false, false, false, false, false).remoteMultiplayer());
            assertFalse(new LodStorageContext(true, false, true, true, false, false).remoteMultiplayer());
        }

        @Test
        @DisplayName("calls a session with either replay mod active a replay")
        void replayDerivation() {
            assertFalse(new LodStorageContext(true, false, true, false, false, false).replay());
            assertTrue(new LodStorageContext(true, false, true, false, true, false).replay());
            assertTrue(new LodStorageContext(true, false, true, false, false, true).replay());
            assertTrue(new LodStorageContext(true, false, true, false, true, true).replay());
        }
    }
}
