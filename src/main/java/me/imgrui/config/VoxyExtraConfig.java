package me.imgrui.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = "voxy-extra")
public class VoxyExtraConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    public boolean fixNetherFog = true;
    @ConfigEntry.Gui.Excluded
    public boolean flashbackSaveOldLods;
    @ConfigEntry.Gui.Excluded
    public boolean flashbackIngest = true;
    @ConfigEntry.Gui.Excluded
    public boolean flashbackCheckLodCache = true;
    @ConfigEntry.Gui.Excluded
    public boolean serverBlacklist;
    @ConfigEntry.Gui.Tooltip
    public List<String> serverBlacklistList = new ArrayList<>();
    @ConfigEntry.Gui.Excluded
    public boolean serverWhitelist;
    @ConfigEntry.Gui.Tooltip
    public List<String> serverWhitelistList = new ArrayList<>();
    @ConfigEntry.Gui.Excluded
    public boolean lodMirror;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<String> lodMirrorList = new ArrayList<>();
    @ConfigEntry.Gui.Excluded
    public boolean disableInSingleplayer;
}
