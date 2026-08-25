package me.imgrui.flashback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.imgrui.VoxyExtra;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static me.imgrui.VoxyExtra.mcPath;

public class FlashbackCopy {
    /**
     * The worlds the recording in progress has visited, added to from the recorder's tick and read again
     * when the recording ends, so it is written and read from different threads and cannot be a plain set.
     * Its clearing points are unchanged: the end of a recording, and a cancelled one.
     */
    public static final Set<String> IDENTIFIERS = ConcurrentHashMap.newKeySet();

    /**
     * Where each world of the current session actually stores its LoDs, filled in as Voxy builds storage.
     * <p>
     * Its lifetime is one Voxy client instance, not one recording: the base path it describes is fixed when
     * that instance is built, and {@link #forgetWorldBases()} drops it when the next one is. It deliberately
     * survives the end of a recording, unlike {@link #IDENTIFIERS}, because Flashback writes a replay's
     * metadata at a moment that moves - inside {@code finishRecordingReplay} when quicksave is on, only once
     * the player has named the replay when it is off - so anything cleared there is sometimes cleared too
     * early to still answer for the recording that just finished.
     */
    private static final Map<String, WorldLodBase> WORLD_BASES = new ConcurrentHashMap<>();

    /**
     * Topics already warned about, so a warning that is decided over and over is said once.
     * <p>
     * Cleared for a new recording and for a new Voxy client instance alike: a reconnection during a
     * recording refills {@link #WORLD_BASES} from scratch, and what is true of the new connection deserves
     * to be said again rather than silenced by a latch set for the old one.
     */
    private static final Set<WarningTopic> WARNED = ConcurrentHashMap.newKeySet();

    public static String replayIdentifier;
    public static Path basePath;
    public static boolean voxySavedLods;

    /** Records where a world of this session really stores its LoDs, as Voxy builds that world's storage. */
    public static void rememberWorldBase(String worldId, Path base, boolean seedKeyed) {
        WORLD_BASES.put(worldId, new WorldLodBase(base, seedKeyed));
    }

    /**
     * Drops the session's bases, which stop meaning anything once Voxy builds a new client instance.
     * <p>
     * The warning latch goes with them: the bases the warnings were decided from are gone, so a problem
     * that is still true of the new connection has to be able to say so once more.
     */
    public static void forgetWorldBases() {
        WORLD_BASES.clear();
        WARNED.clear();
    }

    /** The seed keyed directories this session's LoDs went into, for whoever has to record a single path. */
    public static SessionLodBases sessionLodBases() {
        return SessionLodBases.of(WORLD_BASES);
    }

    /**
     * Drops the replay identity and the warning latch, as Flashback builds the recorder for a new recording.
     * <p>
     * {@link #replayIdentifier} and {@link #basePath} are only ever filled in from a replay's metadata, and
     * Flashback writes that metadata as the recording runs. Left standing they would still name the previous
     * replay for a recording whose metadata was never written, which would have this session's LoDs copied
     * into that replay's directory - or that replay's directory deleted on this one's behalf.
     * <p>
     * It deliberately does not touch {@link #IDENTIFIERS}. Three different lifetimes meet in this class and
     * each is cleared where it ends: the visited worlds by {@code FlashbackMixin} when a recording finishes
     * or is cancelled, the replay identity and warning latch here when the next recording begins, and
     * {@link #WORLD_BASES} by {@link #forgetWorldBases()} when Voxy builds a new client instance.
     */
    public static void startNewRecording() {
        replayIdentifier = null;
        basePath = null;
        WARNED.clear();
    }

    /**
     * Whether {@code topic} still has to be warned about, and from now on it does not.
     * <p>
     * Flashback rewrites a replay's metadata every time it writes a chunk, so a warning decided while
     * building that metadata is decided again every few seconds of recording. This makes it one line.
     */
    public static boolean shouldWarn(WarningTopic topic) {
        return WARNED.add(topic);
    }

    public static void CopyLods() {
        if (replayIdentifier == null || basePath == null) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Flashback recorded no storage path for this replay, so no LoDs are copied; going ahead would have copied them into whichever replay was recorded before it");
            return;
        }
        Path copyPath = mcPath.resolve(".voxy").resolve(CopiedLodPath.FLASHBACK_DIRECTORY).resolve(replayIdentifier);
        LodCopyPlan plan = LodCopyPlan.of(IDENTIFIERS, WORLD_BASES, basePath, copyPath);
        if (plan.voxyLooksSeedAware()) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Voxy now reports the seed keyed storage path itself for {}, so Voxy Extra is redirecting a path that no longer needs it; please report this so the redirect can be retired before it starts copying from the wrong place", plan.seedAwareWorlds());
        }
        LodCopier.Report report = LodCopier.copy(plan, LodCopier.JOURNAL_FILTER, VoxyExtra.LOGGER);
        if (report.copiedNothing()) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Copied no LoDs for {}, nothing was stored for any of its {} worlds", replayIdentifier, plan.entries().size());
            return;
        }
        VoxyExtra.LOGGER.info("[Voxy Extra] Copied LoDs for {}, {} of its {} worlds had something stored", replayIdentifier, report.copied().size(), plan.entries().size());
    }

    public static void CheckReplays() {
        Path replays = mcPath.resolve("flashback").resolve("replays");
        Path flashbackLodFolder = mcPath.resolve(".voxy").resolve(CopiedLodPath.FLASHBACK_DIRECTORY);
        List<Path> flashbackLodFolders = new ArrayList<>();
        if (!Files.exists(flashbackLodFolder)) return;
        if (Files.exists(replays)) {
            try (var stream = Files.walk(replays)) {
                stream
                        .filter(path -> path.toString().endsWith(".zip"))
                        .forEach(zipPath -> {
                            try {
                                String lodUUID = lodUUID(zipPath);
                                if (lodUUID != null) {
                                    flashbackLodFolders.add(flashbackLodFolder.resolve(lodUUID));
                                }
                            } catch (Exception e) {
                                VoxyExtra.LOGGER.warn("[Voxy Extra] Failed to check replay {}", zipPath);
                            }
                        });
            } catch (IOException e) {
                VoxyExtra.LOGGER.warn("[Voxy Extra] Failed to walk replays, stopping check");
                return;
            }
        }

        Set<Path> flashbackLodFoldersSet = new HashSet<>(flashbackLodFolders);

        try (var stream = Files.list(flashbackLodFolder)) {
            stream
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            if (!flashbackLodFoldersSet.contains(path)) {
                                FileUtils.deleteDirectory(path.toFile());
                                VoxyExtra.LOGGER.warn("[Voxy Extra] Deleted permanently {}", path);
                            }
                        } catch (Exception e) {
                            VoxyExtra.LOGGER.error("[Voxy Extra] Failed to delete {}", path, e);
                        }
                    });
        } catch (IOException e) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Failed to walk flashback LoDs files");
        }
    }

    public static void deleteReplayLOD() {
        if (replayIdentifier == null) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Flashback recorded no storage path for this replay, so no LoDs are deleted; going ahead would have deleted those of whichever replay was recorded before it");
            return;
        }
        Path flashbackLod = mcPath.resolve(".voxy").resolve(CopiedLodPath.FLASHBACK_DIRECTORY).resolve(replayIdentifier);
        try {
            FileUtils.deleteDirectory(flashbackLod.toFile());
            VoxyExtra.LOGGER.warn("[Voxy Extra] Deleted LoD for {}", replayIdentifier);
        } catch (IOException e) {
            VoxyExtra.LOGGER.error("[Voxy Extra] Failed to delete LoD for {}", replayIdentifier);
        }
    }

    private static String lodUUID(Path zipPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipEntry zipEntry = zipFile.getEntry("metadata.json");
            if (zipEntry != null) {
                try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    JsonObject jsonObject = JsonParser.parseReader(inputStreamReader).getAsJsonObject();
                    return CopiedLodPath.copiedLodUuid(jsonObject);
                }
            }
        } catch (IOException e) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Failed to read LoD location from {}", zipPath);
        }
        return null;
    }
}
