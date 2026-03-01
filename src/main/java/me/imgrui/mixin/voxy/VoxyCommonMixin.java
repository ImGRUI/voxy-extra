package me.imgrui.mixin.voxy;

import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VoxyCommon.class, remap = false)
public class VoxyCommonMixin {
    @Inject(method = "createInstance", at = @At("HEAD"), cancellable = true)
    private static void voxyExtra$serverBlacklist(CallbackInfo ci) {
        if (VoxyExtraConfig.CONFIG.getServerBlacklist() && VoxyConfig.CONFIG.enabled) {
            var host = VoxyExtra.HOST;
            if (host != null && VoxyExtraConfig.CONFIG.serverBlacklistList.contains(host)) {
                VoxyConfig.CONFIG.enabled = false;
                VoxyClientInstance.isInGame = false;
                ci.cancel();

                VoxyExtra.isInBlacklist = true;
                VoxyExtra.LOGGER.info("[Voxy Extra] Server {} in blacklist, disabling Voxy", host);

                return;
            }
        }

        VoxyExtra.isInBlacklist = false;
    }
}