package me.imgrui.flashback;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure decision behind {@code CheckReplays}: does a replay's recorded Voxy storage
 * path point at the copy this mod made under {@code .voxy/flashback/<uuid>}?
 * <p>
 * Nothing here touches Minecraft, Voxy or the file system: the only input is the metadata document
 * that {@code FlashbackCopy#lodUUID} reads out of a replay's {@code metadata.json}.
 */
class CopiedLodPathTest {

    private static final String UUID = "0f5c8a1e-3b2d-4a77-9c10-8de4b6f0a921";

    /** What a Windows client writes: {@code %APPDATA%\.minecraft\.voxy\flashback\<uuid>}. */
    private static final String WINDOWS_COPY =
            "C:\\Users\\cat\\AppData\\Roaming\\.minecraft\\.voxy\\flashback\\" + UUID;

    /** What a Linux or macOS client writes: {@code ~/.minecraft/.voxy/flashback/<uuid>}. */
    private static final String UNIX_COPY =
            "/home/cat/.minecraft/.voxy/flashback/" + UUID;

    /** Voxy's own address keyed directory, i.e. a replay whose LoDs were never copied. */
    private static final String WINDOWS_ADDRESS =
            "C:\\Users\\cat\\AppData\\Roaming\\.minecraft\\.voxy\\saves\\mc.example.com_25565";

    private static final String UNIX_ADDRESS =
            "/home/cat/.minecraft/.voxy/saves/mc.example.com_25565";

    /** The fields of a replay's {@code metadata.json} this decision reads, and one it must ignore. */
    private static JsonObject metadata(String storagePath) {
        JsonObject meta = new JsonObject();
        meta.addProperty("uuid", UUID);
        meta.addProperty("name", "Some Replay");
        meta.addProperty("voxy_storage_path", storagePath);
        return meta;
    }

    @Nested
    @DisplayName("copiedLodUuid")
    class CopiedLodUuid {

        @Test
        @DisplayName("claims the uuid of a replay whose LoDs were copied on Windows")
        void windowsCopy() {
            assertEquals(UUID, CopiedLodPath.copiedLodUuid(metadata(WINDOWS_COPY)));
        }

        @Test
        @DisplayName("claims the uuid of a replay whose LoDs were copied on Linux or macOS")
        void unixCopy() {
            assertEquals(UUID, CopiedLodPath.copiedLodUuid(metadata(UNIX_COPY)));
        }

        @Test
        @DisplayName("claims nothing for a replay still pointing at voxy's own address directory")
        void addressDirectoryIsNotACopy() {
            assertNull(CopiedLodPath.copiedLodUuid(metadata(WINDOWS_ADDRESS)));
            assertNull(CopiedLodPath.copiedLodUuid(metadata(UNIX_ADDRESS)));
        }

        @Test
        @DisplayName("claims nothing when the replay has no recorded voxy path at all")
        void noStoragePath() {
            JsonObject meta = new JsonObject();
            meta.addProperty("uuid", UUID);
            assertNull(CopiedLodPath.copiedLodUuid(meta));
        }

        @Test
        @DisplayName("claims nothing for a null document")
        void nullDocument() {
            assertNull(CopiedLodPath.copiedLodUuid(null));
        }

        @Test
        @DisplayName("claims nothing when the recorded path is not a string")
        void nonStringStoragePath() {
            JsonObject nullValued = metadata(UNIX_COPY);
            nullValued.add("voxy_storage_path", JsonNull.INSTANCE);
            assertNull(CopiedLodPath.copiedLodUuid(nullValued));

            JsonObject numberValued = metadata(UNIX_COPY);
            numberValued.addProperty("voxy_storage_path", 42);
            assertNull(CopiedLodPath.copiedLodUuid(numberValued));

            // The one input the old substring check and this one disagree about: a composite value whose
            // serialised form happens to contain voxy\\flashback\\. Nothing writes a composite there -
            // the mixin only ever stores a string - and a path that is not a path names no directory to
            // keep, so rejecting it is the correct reading rather than an accident of the rewrite.
            JsonArray array = new JsonArray();
            array.add(WINDOWS_COPY);
            JsonObject arrayValued = metadata(UNIX_COPY);
            arrayValued.add("voxy_storage_path", array);
            assertTrue(array.toString().contains("voxy\\\\flashback\\\\"), "the disagreeing input");
            assertNull(CopiedLodPath.copiedLodUuid(arrayValued));
        }

        @Test
        @DisplayName("claims nothing when the replay's own uuid is missing or not a string")
        void unusableUuid() {
            JsonObject missing = metadata(UNIX_COPY);
            missing.remove("uuid");
            assertNull(CopiedLodPath.copiedLodUuid(missing));

            JsonObject nullValued = metadata(UNIX_COPY);
            nullValued.add("uuid", JsonNull.INSTANCE);
            assertNull(CopiedLodPath.copiedLodUuid(nullValued));

            JsonObject objectValued = metadata(UNIX_COPY);
            objectValued.add("uuid", new JsonObject());
            assertNull(CopiedLodPath.copiedLodUuid(objectValued));
        }
    }

    @Nested
    @DisplayName("pointsAtCopiedLods")
    class PointsAtCopiedLods {

        @Test
        @DisplayName("accepts either separator for the same directory layout")
        void bothSeparators() {
            assertTrue(CopiedLodPath.pointsAtCopiedLods(WINDOWS_COPY));
            assertTrue(CopiedLodPath.pointsAtCopiedLods(UNIX_COPY));
        }

        @Test
        @DisplayName("rejects voxy's address keyed directories")
        void rejectsAddressDirectories() {
            assertFalse(CopiedLodPath.pointsAtCopiedLods(WINDOWS_ADDRESS));
            assertFalse(CopiedLodPath.pointsAtCopiedLods(UNIX_ADDRESS));
        }

        @Test
        @DisplayName("needs a replay directory below flashback, not the flashback directory itself")
        void needsSomethingBelowFlashback() {
            assertFalse(CopiedLodPath.pointsAtCopiedLods("/home/cat/.minecraft/.voxy/flashback"));
            assertFalse(CopiedLodPath.pointsAtCopiedLods("C:\\.minecraft\\.voxy\\flashback"));
            assertTrue(CopiedLodPath.pointsAtCopiedLods("/home/cat/.minecraft/.voxy/flashback/"));
        }

        @Test
        @DisplayName("needs flashback to sit directly inside the voxy directory")
        void needsFlashbackDirectlyBelowVoxy() {
            assertFalse(CopiedLodPath.pointsAtCopiedLods("/home/cat/.voxy/saves/flashback/" + UUID));
            assertFalse(CopiedLodPath.pointsAtCopiedLods("/home/cat/flashback/.voxy/" + UUID));
        }

        @Test
        @DisplayName("accepts any directory merely ending in voxy, as the old substring check did")
        void suffixMatchIsDeliberate() {
            // Tightening this to an exact .voxy match would be a behaviour change on paths the old check
            // accepted, which this fix is not allowed to make. Pinned so the change cannot happen silently.
            assertTrue(CopiedLodPath.pointsAtCopiedLods("/home/cat/.notvoxy/flashback/" + UUID));
            assertTrue(CopiedLodPath.pointsAtCopiedLods("C:\\cat\\.notvoxy\\flashback\\" + UUID));
        }

        @Test
        @DisplayName("does not read the separator of one style as a character of the other")
        void mixedSeparators() {
            assertTrue(CopiedLodPath.pointsAtCopiedLods("/home/cat/.voxy\\flashback/" + UUID));
        }

        @Test
        @DisplayName("rejects a path with no directory structure at all")
        void degenerateInput() {
            assertFalse(CopiedLodPath.pointsAtCopiedLods(""));
            assertFalse(CopiedLodPath.pointsAtCopiedLods(".voxyflashback" + UUID));
        }
    }
}
