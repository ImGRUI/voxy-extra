package me.imgrui;

import com.mojang.blaze3d.platform.InputConstants;
import me.cortex.voxy.client.ClientSessionEvents;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.IVoxyRenderSystemHolder;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class VoxyExtra implements ModInitializer {
	public static final String MOD_ID = "voxy-extra";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean isFlashbackLoaded = FabricLoader.getInstance().isModLoaded("flashback");
	public static final boolean isReplayModLoaded = FabricLoader.getInstance().isModLoaded("replaymod");
	public static final Path mcPath = Minecraft.getInstance().gameDirectory.toPath();
	public static VoxyExtraConfig CONFIG;

	public static boolean isVoxyDisabled;

	KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("voxy-extra","keybinds"));
	KeyMapping toggleVoxy = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.voxy-extra.toggleVoxy", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));
	KeyMapping toggleVoxyRendering = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.voxy-extra.toggleVoxyRendering", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));

	@Override
	public void onInitialize() {
        LOGGER.info("[Voxy Extra] Loading Voxy Extra");
		AutoConfig.register(VoxyExtraConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(VoxyExtraConfig.class).getConfig();
		if (CONFIG.flashbackCheckLodCache && isFlashbackLoaded) FlashbackCopy.CheckReplays();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (toggleVoxy.consumeClick()) {
				if (client.level == null) return;
				VoxyConfig.CONFIG.enabled = !VoxyConfig.CONFIG.enabled;
				voxyFullReload();
			}
			if (toggleVoxyRendering.consumeClick()) {
				if (client.level == null) return;
				if (VoxyConfig.CONFIG.enabled) {
					VoxyConfig.CONFIG.enableRendering = !VoxyConfig.CONFIG.enableRendering;
					voxyRendererReload();
				} else {
					Minecraft.getInstance().gui.chatListener().handleSystemMessage(Component.literal("Voxy Extra: Voxy is not enabled, toggling rendering is disabled"), false);
				}
			}
		});
	}

	private void voxyFullReload() {
		var vrsh = IVoxyRenderSystemHolder.getNullableHolder();
		if (vrsh != null) {
			vrsh.voxy$shutdownRenderer();
		}
		VoxyCommon.shutdownInstance();
		if (VoxyConfig.CONFIG.enabled && ClientSessionEvents.inSession) {
			VoxyCommon.createInstance();
		}
		Minecraft.getInstance().levelExtractor.allChanged();
		IrisUtil.reload();
	}

	private void voxyRendererReload() {
		var vrsh = IVoxyRenderSystemHolder.getNullableHolder();
		if (vrsh != null) {
			if (VoxyConfig.CONFIG.enableRendering) {
				vrsh.voxy$createRenderer();
			} else {
				vrsh.voxy$shutdownRenderer();
			}
		}
		IrisUtil.reload();
	}
}