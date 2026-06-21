package me.imgrui.config;

import me.shedaniel.autoconfig.AutoConfig;

public class VoxyExtraStorage {

    public VoxyExtraClothConfig config() {
        return AutoConfig.getConfigHolder(VoxyExtraClothConfig.class).getConfig();
    }

    public void save() {
        AutoConfig.getConfigHolder(VoxyExtraClothConfig.class).save();
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
        return config().saveOldLods;
    }

    public void setSaveOldLods(boolean saveOldLods) {
        config().saveOldLods = saveOldLods;
    }

    public boolean getServerBlacklist() {
        return config().serverBlacklist;
    }

    public void setServerBlacklist(boolean serverBlacklist) {
        config().serverBlacklist = serverBlacklist;
    }

    public boolean getLodMirror() {
        return config().lodMirror;
    }

    public void setLodMirror(boolean lodMirror) {
        config().lodMirror = lodMirror;
    }
}
