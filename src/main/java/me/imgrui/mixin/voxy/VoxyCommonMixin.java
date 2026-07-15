package me.imgrui.mixin.voxy;

import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.imgrui.VoxyExtra;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VoxyCommon.class, remap = false)
public class VoxyCommonMixin {
    @Inject(method = "createInstance", at = @At("HEAD"), cancellable = true)
    private static void voxyExtra$serverListCheck(CallbackInfo ci) {
        if (VoxyConfig.CONFIG.enabled && !VoxyExtra.isDisabledByServerList) {
            String ip = VoxyExtra.IP;
            
            if (ip != null) {
                boolean isBlacklisted = VoxyExtra.CONFIG.serverBlacklist && VoxyExtra.CONFIG.serverBlacklistList.contains(ip);
                boolean isNotWhitelisted = VoxyExtra.CONFIG.serverWhitelist && !VoxyExtra.CONFIG.serverWhitelistList.contains(ip);

                if (isBlacklisted) {
                    VoxyExtra.LOGGER.warn("[Voxy Extra] Server {} is blacklisted, disabling Voxy", ip);
                } else if (isNotWhitelisted) {
                    VoxyExtra.LOGGER.warn("[Voxy Extra] Server {} is not whitelisted, disabling Voxy", ip);
                }

                if (isBlacklisted || isNotWhitelisted) {
                    VoxyConfig.CONFIG.enabled = false;
                    ci.cancel();
                    IrisUtil.reload();
                    VoxyExtra.isDisabledByServerList = true;
                    return;
                }
            }
        }
        VoxyExtra.isDisabledByServerList = false;
    }
}