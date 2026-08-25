package me.imgrui.flashback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The state {@code FlashbackCopy} carries between the moments a recording passes through, and the two
 * points that drop it: a new recording, and a new Voxy client instance.
 * <p>
 * Only that state is exercised here. {@code FlashbackCopy} loads without a game around it as long as no
 * Minecraft backed field of {@code VoxyExtra} is read, which none of these methods does.
 */
class FlashbackCopyStateTest {

    private static final Path SEED_BASE = Path.of("/game/.voxy/saves/world-0000000000001234");
    private static final String OVERWORLD = "minecraft_overworld";

    @BeforeEach
    void freshSession() {
        FlashbackCopy.forgetWorldBases();
        FlashbackCopy.startNewRecording();
    }

    @Nested
    @DisplayName("shouldWarn")
    class ShouldWarn {

        @Test
        @DisplayName("lets a topic through once and then holds it back")
        void oncePerRecording() {
            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
            assertFalse(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
            assertFalse(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
        }

        @Test
        @DisplayName("keeps one topic from silencing another")
        void topicsAreIndependent() {
            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));

            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.PATH_OUTSIDE_SEED_STORAGE));
            assertFalse(FlashbackCopy.shouldWarn(WarningTopic.PATH_OUTSIDE_SEED_STORAGE));
            assertFalse(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
        }
    }

    @Nested
    @DisplayName("startNewRecording")
    class StartNewRecording {

        @Test
        @DisplayName("drops the previous replay's identity so a new recording cannot inherit it")
        void forgetsThePreviousReplay() {
            FlashbackCopy.replayIdentifier = "0f5c8a1e-3b2d-4a77-9c10-8de4b6f0a921";
            FlashbackCopy.basePath = Path.of("/game/.voxy/saves/mc.example.com_25565");

            FlashbackCopy.startNewRecording();

            assertNull(FlashbackCopy.replayIdentifier);
            assertNull(FlashbackCopy.basePath);
        }

        @Test
        @DisplayName("says a warning again for the next recording")
        void resetsTheLatch() {
            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
            assertFalse(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));

            FlashbackCopy.startNewRecording();

            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
        }

        @Test
        @DisplayName("leaves the observed bases alone, since the connection did not change")
        void keepsTheObservedBases() {
            FlashbackCopy.rememberWorldBase(OVERWORLD, SEED_BASE, true);

            FlashbackCopy.startNewRecording();

            assertTrue(FlashbackCopy.sessionLodBases().seedKeyingApplied());
        }
    }

    @Nested
    @DisplayName("forgetWorldBases")
    class ForgetWorldBases {

        @Test
        @DisplayName("drops the observed bases when voxy builds a new client instance")
        void forgetsTheObservedBases() {
            FlashbackCopy.rememberWorldBase(OVERWORLD, SEED_BASE, true);
            assertTrue(FlashbackCopy.sessionLodBases().seedKeyingApplied());

            FlashbackCopy.forgetWorldBases();

            assertFalse(FlashbackCopy.sessionLodBases().seedKeyingApplied());
        }

        @Test
        @DisplayName("lets a reconnection during a recording be warned about again")
        void reconnectionSpeaksUpAgain() {
            // The bases the first warning was decided from are gone; whatever is true of the worlds seen
            // after reconnecting has not been said yet and must not be silenced by the old latch.
            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
            assertFalse(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));

            FlashbackCopy.forgetWorldBases();

            assertTrue(FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES));
        }
    }
}
