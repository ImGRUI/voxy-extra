package me.imgrui.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class VoxyExtraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.STATIC).create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("voxy-extra.json");

    public static final VoxyExtraConfig CONFIG = VoxyExtraConfig.load();

    public boolean fixNetherFog;
    public boolean saveOldLods;
    public boolean flashbackIngest;
    public boolean serverBlacklist;
    public ArrayList<String> serverBlacklistList = new ArrayList<>();
    public boolean lodMirror;
    public ArrayList<ArrayList<String>> lodMirrorList = new ArrayList<>();

    public VoxyExtraConfig() {
        fixNetherFog = true;
        saveOldLods = false;
        flashbackIngest = false;
        serverBlacklist = false;
        lodMirror = false;
    }

    public void save() {
        try {
            write();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    public static VoxyExtraConfig load() {
        VoxyExtraConfig config;

        if (Files.exists(CONFIG_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                config = GSON.fromJson(reader, VoxyExtraConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse Voxy Extra config", e);
            }
        } else {
            config = new VoxyExtraConfig();
        }

        try {
            config.write();
        } catch (IOException e) {
            throw new RuntimeException("Could not parse Voxy Extra config", e);
        }

        return config;
    }

    private void write() throws IOException {
        Path path = CONFIG_PATH.getParent();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Files.writeString(CONFIG_PATH, GSON.toJson(this));
    }

    public boolean getFlashbackIngest() {
        return flashbackIngest;
    }

    public void setFlashbackIngest(boolean flashbackIngest) {
        this.flashbackIngest = flashbackIngest;
    }

    public boolean getFixNetherFog() {
        return fixNetherFog;
    }

    public void setFixNetherFog(boolean fixNetherFog) {
        this.fixNetherFog = fixNetherFog;
    }

    public boolean getSaveOldLods() {
        return saveOldLods;
    }

    public void setSaveOldLods(boolean saveOldLods) {
        this.saveOldLods = saveOldLods;
    }

    public boolean getServerBlacklist() {
        return serverBlacklist;
    }

    public void setServerBlacklist(boolean serverBlacklist) {
        this.serverBlacklist = serverBlacklist;
    }

    public boolean getLodMirror() {
        return lodMirror;
    }

    public void setLodMirror(boolean lodMirror) {
        this.lodMirror = lodMirror;
    }

}
