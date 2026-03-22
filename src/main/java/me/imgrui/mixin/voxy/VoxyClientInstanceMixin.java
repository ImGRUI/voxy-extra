package me.imgrui.mixin.voxy;

import com.llamalad7.mixinextras.sugar.Local;
import me.cortex.voxy.client.VoxyClientInstance;
import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

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

    @ModifyVariable(method = "<init>()V", at = @At(value = "INVOKE_ASSIGN", target = "Lme/cortex/voxy/client/VoxyClientInstance;getBasePath()Ljava/nio/file/Path;"), name = "path")
    private Path voxyExtra$lodMirror(Path path) {
        return voxyExtra$lodMirrorCheck(path);
    }

    @Unique
    private Path voxyExtra$lodMirrorCheck(Path path) {
        if (!VoxyExtraConfig.CONFIG.getLodMirror()) return path;
        if (VoxyExtraConfig.CONFIG.lodMirrorMap.isEmpty()) return path;

        String currentHost = VoxyExtra.IP;
        if (currentHost == null) return path;

        for (Map.Entry<String, Set<String>> entry : VoxyExtraConfig.CONFIG.lodMirrorMap.entrySet()) {
            String primaryHost = entry.getKey();
            Set<String> linkedHosts = entry.getValue();

            if (primaryHost.equals(currentHost)) return path;

            if (linkedHosts.contains(currentHost)) {
                path = path.resolveSibling(primaryHost);
                VoxyExtra.LOGGER.warn("[Voxy Extra] Successfully replaced path to {}", primaryHost);
                break;
            }
        }

        return path;
    }
}
