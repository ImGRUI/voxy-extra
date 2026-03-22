package me.imgrui.mixin.voxy;

import me.cortex.voxy.client.ClientSessionEvents;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.util.IrisUtil;
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
            var IP = VoxyExtra.IP;
            if (IP != null && VoxyExtraConfig.CONFIG.serverBlacklistList.contains(IP)) {
                VoxyConfig.CONFIG.enabled = false;
                ClientSessionEvents.inSession = false;
                ci.cancel();
                IrisUtil.reload();
                VoxyExtra.isInBlacklist = true;
                VoxyExtra.LOGGER.warn("[Voxy Extra] Server {} in blacklist, disabling Voxy", IP);
                return;
            }
        }
        VoxyExtra.isInBlacklist = false;
    }
}