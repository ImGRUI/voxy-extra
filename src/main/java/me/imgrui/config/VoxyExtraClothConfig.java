package me.imgrui.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Config(name = "voxy-extra")
public class VoxyExtraClothConfig implements ConfigData {
    public boolean fixNetherFog = true;
    public boolean saveOldLods;
    public boolean flashbackIngest;
    public boolean serverBlacklist;
    public List<String> serverBlacklistList = new ArrayList<>();
    public boolean lodMirror;
    public List<String> lodMirrorList = new ArrayList<>();
}
