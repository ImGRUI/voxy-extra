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
        path = voxyExtra$lodMirrorCheck(path);
        return path;
    }

    @Unique
    private Path voxyExtra$lodMirrorCheck(Path path) {
        if (VoxyExtraConfig.CONFIG.lodMirrorList.isEmpty()) return path;
        if (VoxyExtraConfig.CONFIG.getLodMirror()) {
            String value = path.toString().replace("\\", "/");
            String ip = value.substring(value.lastIndexOf("/") + 1).replace("_", ":");
            for (int i = 0; i < VoxyExtraConfig.CONFIG.lodMirrorList.size(); i++) {
                var list = VoxyExtraConfig.CONFIG.lodMirrorList.get(i);
                if (list.getFirst().equals(ip)) return path;
                if (list.contains(ip)) {
                    String listf = list.getFirst();
                    path = path.resolveSibling(listf);
                    VoxyExtra.LOGGER.info("[Voxy Extra] Successfully replaced path to {}", listf);
                    break;
                }
            }
        }
        return path;
    }
}
