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

    public boolean getDisableInSingleplayer() {
        return config().disableInSingleplayer;
    }

    public void setDisableInSingleplayer(boolean disableInSingleplayer) {
        config().disableInSingleplayer = disableInSingleplayer;
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

    public boolean getReplayModSaveCustomMeta() {
        return config().replayModSaveCustomMeta;
    }

    public void setReplayModSaveCustomMeta(boolean replayModSaveCustomMeta) {
        config().replayModSaveCustomMeta = replayModSaveCustomMeta;
    }

    public boolean getReplayModLoadLods() {
        return config().replayModLoadLods;
    }

    public void setReplayModLoadLods(boolean replayModLoadLods) {
        config().replayModLoadLods = replayModLoadLods;
    }

    public boolean getCustomFogFade() {
        return config().customFogFade;
    }

    public void setCustomFogFade(boolean customFogFade) {
        config().customFogFade = customFogFade;
    }

    public int getFogStart() {
        return config().fadeFogStart;
    }

    public void setFogStart(int fogStart) {
        config().fadeFogStart = fogStart;
    }

    public int getFogEnd() {
        return config().fadeFogEnd;
    }

    public void setFogEnd(int fogEnd) {
        config().fadeFogEnd = fogEnd;
    }
}