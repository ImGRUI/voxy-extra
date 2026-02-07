package me.imgrui.flashback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.imgrui.VoxyExtra;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlashbackCopy {
    public static HashSet<String> IDENTIFIERS = new HashSet<>();
    public static String replayIdentifier;
    public static Path basePath;
    public static boolean voxySavedLods;

    static FileFilter filter = file -> !file.getName().contains("LOG") && !file.getName().equals("LOCK");

    public static void CopyLods() {
        Path copyPath = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy").resolve("flashback").resolve(replayIdentifier);
        CopyLods(basePath, copyPath);
    }

    private static void CopyLods(Path basePath, Path copyPath) {
        for (String worldId : IDENTIFIERS) {
            Path newBasePath = basePath.resolve(worldId);
            Path newCopyPath = copyPath.resolve(worldId);
            try {
                FileUtils.copyDirectory(newBasePath.toFile(), newCopyPath.toFile(), filter);
                FileUtils.copyFile(basePath.resolve("config.json").toFile(), copyPath.resolve("config.json").toFile());
            } catch (IOException e) {
                VoxyExtra.LOGGER.error("[Voxy Extra] Failed to copy lods for {}", replayIdentifier, e);
                return;
            }
        }
        VoxyExtra.LOGGER.info("[Voxy Extra] Copied LoDs for {}", replayIdentifier);
    }

    public static void CheckReplays() {
        Path replays = Minecraft.getInstance().gameDirectory.toPath().resolve("flashback").resolve("replays");
        Path flashbackLodFolder = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy").resolve("flashback");
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
                VoxyExtra.LOGGER.warn("[Voxy Extra] Failed to walk replays files, stopping check");
                return;
            }
        }

        try (var stream = Files.list(flashbackLodFolder)) {
            stream
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            if (!flashbackLodFolders.contains(path)) {
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

    private static String lodUUID(Path zipPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipEntry zipEntry = zipFile.getEntry("metadata.json");
            if (zipEntry != null) {
                try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    JsonObject jsonObject = JsonParser.parseReader(inputStreamReader).getAsJsonObject();
                    if (jsonObject.get("voxy_storage_path").toString().contains("voxy\\\\flashback\\\\")) {
                        return jsonObject.get("uuid").getAsString();
                    }
                }
            }
        } catch (IOException e) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] Failed to read LoD location from {}", zipPath);
        }
        return null;
    }
}
