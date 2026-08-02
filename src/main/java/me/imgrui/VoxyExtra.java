package me.imgrui;

import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoxyExtra implements ModInitializer {
	public static final String MOD_ID = "voxy-extra";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean isFlashbackLoaded = FabricLoader.getInstance().isModLoaded("flashback");
	public static final boolean isReplayModLoaded = FabricLoader.getInstance().isModLoaded("replaymod");
	public static VoxyExtraConfig CONFIG;

    public static boolean isVoxyDisabled;
	public static volatile @Nullable String IP;

	@Override
	public void onInitialize() {
        LOGGER.info("[Voxy Extra] Loading Voxy Extra");
		AutoConfig.register(VoxyExtraConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(VoxyExtraConfig.class).getConfig();
		if (CONFIG.flashbackCheckLodCache && isFlashbackLoaded) FlashbackCopy.CheckReplays();
	}
}