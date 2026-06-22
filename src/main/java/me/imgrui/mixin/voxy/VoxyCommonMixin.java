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
    private static void voxyExtra$serverBlacklist(CallbackInfo ci) {
        if (VoxyExtra.CONFIG.serverBlacklist && VoxyConfig.CONFIG.enabled) {
            var IP = VoxyExtra.IP;
            if (IP != null && VoxyExtra.BlacklistSet.contains(IP)) {
                VoxyConfig.CONFIG.enabled = false;
                ci.cancel();
                IrisUtil.reload();
                VoxyExtra.isInBlacklist = true;
                VoxyExtra.LOGGER.warn("[Voxy Extra] Server {} is blacklisted, disabling Voxy", IP);
                return;
            }
        }
        VoxyExtra.isInBlacklist = false;
    }
}
