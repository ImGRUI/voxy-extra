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

    public static String replayIdentifier;
    public static Path basePath;
    public static boolean voxySavedLods;

    /** Records where a world of this session really stores its LoDs, as Voxy builds that world's storage. */
    public static void rememberWorldBase(String worldId, Path base, boolean seedKeyed) {
        WORLD_BASES.put(worldId, new WorldLodBase(base, seedKeyed));
    }

    /** Drops the session's bases, which stop meaning anything once Voxy builds a new client instance. */
    public static void forgetWorldBases() {
        WORLD_BASES.clear();
    }

    public static void CopyLods() {
        if (replayIdentifier == null || basePath == null) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Flashback has not written this replay's metadata yet, so there is nowhere to copy LoDs to");
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
