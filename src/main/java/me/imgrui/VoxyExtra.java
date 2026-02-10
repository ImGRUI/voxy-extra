package me.imgrui;

import me.imgrui.flashback.FlashbackCopy;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoxyExtra implements ModInitializer {
	public static final String MOD_ID = "voxy-extra";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean isInBlacklist;

	@Override
	public void onInitialize() {
        LOGGER.info("[Voxy Extra] Loading Voxy Extra");
        FlashbackCopy.CheckReplays();
	}
}