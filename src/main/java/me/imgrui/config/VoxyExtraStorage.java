package me.imgrui.config;

import me.shedaniel.autoconfig.AutoConfig;

public class VoxyExtraStorage {

    public VoxyExtraConfig config() {
        return AutoConfig.getConfigHolder(VoxyExtraConfig.class).getConfig();
    }

    public void save() {
        AutoConfig.getConfigHolder(VoxyExtraConfig.class).save();
    }

    public boolean getFlashbackIngest() {
        return config().flashbackIngest;
    }

    public void setFlashbackIngest(boolean flashbackIngest) {
        config().flashbackIngest = flashbackIngest;
    }

    public boolean getFixNetherFog() {
        return config().fixNetherFog;
    }

    public void setFixNetherFog(boolean fixNetherFog) {
        config().fixNetherFog = fixNetherFog;
    }

    public boolean getSaveOldLods() {
        return config().flashbackSaveOldLods;
    }

    public void setSaveOldLods(boolean saveOldLods) {
        config().flashbackSaveOldLods = saveOldLods;
    }

    public boolean getServerBlacklist() {
        return config().serverBlacklist;
    }

    public void setServerBlacklist(boolean serverBlacklist) {
        config().serverBlacklist = serverBlacklist;
    }

    public boolean getServerWhitelist() {
        return config().serverWhitelist;
    }

    public void setServerWhitelist(boolean serverWhitelist) {
        config().serverWhitelist = serverWhitelist;
    }

    public boolean getLodMirror() {
        return config().lodMirror;
    }

    public void setLodMirror(boolean lodMirror) {
        config().lodMirror = lodMirror;
    }

    public boolean getFlashbackCheckLodCache() {
        return config().flashbackCheckLodCache;
    }

    public void setFlashbackCheckLodCache(boolean flashbackCheckLodCache) {
        config().flashbackCheckLodCache = flashbackCheckLodCache;
    }
}
