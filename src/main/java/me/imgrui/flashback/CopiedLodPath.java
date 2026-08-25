package me.imgrui.flashback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.regex.Pattern;

/**
 * Recognises the LoD directory this mod copies a Flashback replay's terrain into.
 * <p>
 * {@link FlashbackCopy#CopyLods()} copies a replay's LoDs to {@code .voxy/flashback/<replay uuid>} and
 * {@code FlashbackMetaMixin} writes that destination into the replay's {@code metadata.json} as
 * {@code voxy_storage_path}. {@link FlashbackCopy#CheckReplays()} runs on every launch and deletes every
 * directory under {@code .voxy/flashback} that no surviving replay claims, so a replay whose recorded path
 * is not recognised here loses its copied LoDs permanently.
 * <p>
 * Recognition is therefore a pure function of the metadata document, testable without Minecraft, Voxy or a
 * file system, and - the point of this class - independent of which separator the recording client wrote.
 * The verdict on a path a Windows client produced, i.e. a backslash separated one, is unchanged; what is new
 * is that a slash separated path is now recognised too, on Windows as well as everywhere else.
 */
public final class CopiedLodPath {

    /** The directory {@link FlashbackCopy} copies into, one level below Voxy's own storage root. */
    public static final String FLASHBACK_DIRECTORY = "flashback";

    /**
     * Voxy's storage root is {@code .voxy}, and a suffix match is what the previous substring check
     * accepted, so keeping it here leaves the verdict on every backslash separated path exactly as it was.
     */
    private static final String VOXY_DIRECTORY_SUFFIX = "voxy";

    /** Both separators, so a path recorded on either platform is read the same way on either platform. */
    private static final Pattern SEPARATORS = Pattern.compile("[/\\\\]");

    private CopiedLodPath() {}

    /**
     * The replay uuid whose copied LoDs the replay in {@code metadata} still owns.
     *
     * @return that uuid, or {@code null} when the replay's LoDs were never copied - which is exactly when
     * {@link FlashbackCopy#CheckReplays()} has nothing to keep on its behalf
     */
    public static String copiedLodUuid(JsonObject metadata) {
        if (metadata == null) return null;
        JsonElement storagePath = metadata.get("voxy_storage_path");
        if (storagePath == null || !storagePath.isJsonPrimitive()) return null;
        if (!pointsAtCopiedLods(storagePath.getAsString())) return null;
        JsonElement uuid = metadata.get("uuid");
        if (uuid == null || !uuid.isJsonPrimitive()) return null;
        return uuid.getAsString();
    }

    /**
     * Whether {@code storagePath} names something inside {@code .voxy/flashback/}, whichever separator it
     * was written with. The path is compared segment by segment rather than as raw text, so neither a
     * Windows backslash nor a Unix slash can hide the layout from the other platform.
     */
    public static boolean pointsAtCopiedLods(String storagePath) {
        if (storagePath == null) return false;
        String[] segments = SEPARATORS.split(storagePath, -1);
        // i + 2 has to exist: the copy lives in a replay directory below flashback, not in flashback itself.
        for (int i = 0; i + 2 < segments.length; i++) {
            if (segments[i].endsWith(VOXY_DIRECTORY_SUFFIX) && segments[i + 1].equals(FLASHBACK_DIRECTORY)) {
                return true;
            }
        }
        return false;
    }
}
