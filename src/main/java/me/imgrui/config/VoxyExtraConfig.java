package me.imgrui.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.imgrui.VoxyExtra;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class VoxyExtraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.STATIC).create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("voxy-extra.json");

    public static final VoxyExtraConfig CONFIG = VoxyExtraConfig.load();

    public boolean netherFogEnabled;
    public int netherFogStartMultiplier;
    public int netherFogEndMultiplier;
    public boolean flashbackCopyLodsEnabled;
    public boolean flashbackIngestEnabled;
    public boolean linkedServersEnabled;
    public Map<String, Set<String>> linkedServers = new LinkedHashMap<>();
    public boolean serverBlacklistEnabled;
    public Set<String> serverBlacklist = new LinkedHashSet<>();
    
    public VoxyExtraConfig() {
        this.netherFogEnabled = true;
        this.netherFogStartMultiplier = 100;
        this.netherFogEndMultiplier = 100;
        this.flashbackCopyLodsEnabled = false;
        this.flashbackIngestEnabled = false;
        this.linkedServersEnabled = false;
        this.serverBlacklistEnabled = false;
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
            } catch (Exception e) {
                VoxyExtra.LOGGER.error("Could not parse Voxy Extra config, resetting", e);
                config = new VoxyExtraConfig();
            }
        } else {
            config = new VoxyExtraConfig();
        }

        try {
            config.write();
        } catch (IOException e) {
            throw new RuntimeException("Could not write Voxy Extra config", e);
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

    public static void openConfig() {
        Util.getPlatform().openFile(new File(CONFIG_PATH.toString()));
    }

    public boolean isNetherFogEnabled() {
        return netherFogEnabled;
    }

    public void setNetherFogEnabled(Boolean value) {
        this.netherFogEnabled = value;
    }

    public int getNetherFogStartMultiplier() {
        return netherFogStartMultiplier;
    }

    public void setNetherFogStartMultiplier(Integer multiplier) {
        this.netherFogStartMultiplier = multiplier;
    }

    public int getNetherFogEndMultiplier() {
        return netherFogEndMultiplier;
    }

    public void setNetherFogEndMultiplier(Integer multiplier) {
        this.netherFogEndMultiplier = multiplier;
    }

    public boolean isFlashbackCopyLodsEnabled() { 
        return flashbackCopyLodsEnabled; 
    }
    
    public void setFlashbackCopyLodsEnable(Boolean value) { 
        this.flashbackCopyLodsEnabled = value; 
    }

    public boolean isFlashbackIngestEnabled() { 
        return flashbackIngestEnabled; 
    }
    
    public void setFlashbackIngestEnabled(Boolean value) { 
        this.flashbackIngestEnabled = value; 
    }

    public boolean isLinkedServersEnabled() {
        return linkedServersEnabled;
    }

    public void setLinkedServersEnabled(Boolean value) {
        this.linkedServersEnabled = value;
    }

    public boolean isServerBlacklistEnabled() {
        return serverBlacklistEnabled;
    }

    public void setServerBlacklistEnabled(Boolean value) {
        this.serverBlacklistEnabled = value;
    }
}