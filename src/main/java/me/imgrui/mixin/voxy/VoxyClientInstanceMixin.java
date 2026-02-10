package me.imgrui.mixin.voxy;

import com.llamalad7.mixinextras.sugar.Local;
import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(value = VoxyClientInstance.class, remap = false)
public class VoxyClientInstanceMixin {
    @Shadow
    @Final
    @Mutable
    private boolean noIngestOverride;

    @Inject(method = "<init>()V", at = @At(value = "FIELD", target = "Lme/cortex/voxy/client/VoxyClientInstance;noIngestOverride:Z", opcode = Opcodes.PUTFIELD))
    private void voxyExtra$flashbackIngest(CallbackInfo ci, @Local(name = "path") Path path) {
        if (VoxyExtraConfig.CONFIG.getFlashbackIngest()) {
            this.noIngestOverride = path != null && !FlashbackCopy.voxySavedLods;
        } else {
            this.noIngestOverride = path != null;
        }
    }

    @Inject(method = "<init>()V", at = @At(value = "INVOKE_ASSIGN", target = "Lme/cortex/voxy/client/VoxyClientInstance;getBasePath()Ljava/nio/file/Path;"))
    private void voxyExtra$serverBlacklist(CallbackInfo ci, @Local(name = "path") Path path) {
        if (VoxyExtraConfig.CONFIG.getServerBlacklist() && VoxyConfig.CONFIG.enabled) {
            String value = path.toString();
            String ip = value.substring(value.lastIndexOf("\\") + 1).replace("_", ":");
            if (VoxyExtraConfig.CONFIG.serverBlacklistList.contains(ip)) {
                VoxyExtra.LOGGER.info("[Voxy Extra] Server {} in blacklist, disabling Voxy", ip);
                VoxyConfig.CONFIG.enabled = false;
                VoxyExtra.isInBlacklist = true;
            }
        } else {
            VoxyExtra.isInBlacklist = false;
        }
    }
}
