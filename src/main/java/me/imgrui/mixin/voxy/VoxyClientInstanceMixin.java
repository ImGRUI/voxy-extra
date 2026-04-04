package me.imgrui.mixin.voxy;

import com.llamalad7.mixinextras.sugar.Local;
import me.cortex.voxy.client.VoxyClientInstance;
import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(value = VoxyClientInstance.class, remap = false)
public class VoxyClientInstanceMixin {
    @Shadow
    @Final
    @Mutable
    private boolean noIngestOverride;

    @Redirect(method = "<init>()V", at = @At(value = "FIELD", target = "Lme/cortex/voxy/client/VoxyClientInstance;noIngestOverride:Z", opcode = Opcodes.PUTFIELD))
    private void voxyExtra$flashbackIngest(VoxyClientInstance instance, boolean value, @Local(name = "path") Path path) {
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
        if (VoxyExtraConfig.CONFIG.lodMirrorList.isEmpty()) return path;
        var IP = VoxyExtra.IP;
        if (IP == null) return path;
        for (int i = 0; i < VoxyExtraConfig.CONFIG.lodMirrorList.size(); i++) {
            var list = VoxyExtraConfig.CONFIG.lodMirrorList.get(i);
            var listFirst = list.getFirst();
            if (listFirst.equals(IP)) return path;
            if (list.contains(IP)) {
                path = path.resolveSibling(listFirst);
                VoxyExtra.LOGGER.warn("[Voxy Extra] Successfully replaced path to {}", listFirst);
                break;
            }
        }
        return path;
    }
}
