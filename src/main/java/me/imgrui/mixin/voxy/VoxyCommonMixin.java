package me.imgrui.mixin.voxy;

import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.imgrui.VoxyExtra;
import me.imgrui.replay.ReplayCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;

@Mixin(value = VoxyCommon.class, remap = false)
public class VoxyCommonMixin {
    @Inject(method = "createInstance", at = @At("HEAD"), cancellable = true)
    private static void voxyExtra$serverListCheck(CallbackInfo ci) {
        if (VoxyConfig.CONFIG.enabled && !VoxyExtra.isVoxyDisabled) {
            boolean isSingleplayer = Minecraft.getInstance().isLocalServer();
            boolean disableForSingleplayer = isSingleplayer && ReplayCompat.getFlashbackReplayServer() == null && VoxyExtra.CONFIG.disableInSingleplayer;

            boolean isBlacklisted = false;
            boolean isNotWhitelisted = false;
            String IP = VoxyExtra.IP;

            if (IP != null && !isSingleplayer) {
                isBlacklisted = VoxyExtra.CONFIG.serverBlacklist && VoxyExtra.CONFIG.serverBlacklistList.contains(IP);
                isNotWhitelisted = VoxyExtra.CONFIG.serverWhitelist && !VoxyExtra.CONFIG.serverWhitelistList.contains(IP);
            }

            if (disableForSingleplayer) {
                VoxyExtra.LOGGER.warn("[Voxy Extra] Singleplayer is disabled, disabling Voxy");
            } else if (isNotWhitelisted) {
                VoxyExtra.LOGGER.warn("[Voxy Extra] Server {} is not whitelisted, disabling Voxy", IP);
            } else if (isBlacklisted) {
                VoxyExtra.LOGGER.warn("[Voxy Extra] Server {} is blacklisted, disabling Voxy", IP);
            }

            if (disableForSingleplayer || isBlacklisted || isNotWhitelisted) {
                VoxyConfig.CONFIG.enabled = false;
                ci.cancel();
                IrisUtil.reload();
                VoxyExtra.isVoxyDisabled = true;
                return;
            }
        }
        VoxyExtra.isVoxyDisabled = false;
    }
}