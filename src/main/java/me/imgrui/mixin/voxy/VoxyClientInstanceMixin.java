package me.imgrui.mixin.voxy;

import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.compat.FlashbackCompat;
import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
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

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void voxyExtra$flashbackIngest(CallbackInfo ci) {
        Path path = FlashbackCompat.getReplayStoragePath(); // If anyone knows how to do this better tell me
        if (VoxyExtraConfig.CONFIG.getFlashbackIngest()) {
            this.noIngestOverride = path != null && !FlashbackCopy.voxySavedLods;
        }
    }

    @ModifyVariable(method = "<init>()V", at = @At(value = "INVOKE_ASSIGN", target = "Lme/cortex/voxy/client/VoxyClientInstance;getBasePath()Ljava/nio/file/Path;"), name = "path")
    private static Path voxyExtra$lodMirror(Path path) {
        return voxyExtra$lodMirrorCheck(path);
    }

    @Unique
    private static Path voxyExtra$lodMirrorCheck(Path path) {
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
                VoxyExtra.LOGGER.warn("[Voxy Extra] Replaced path to {}", listFirst);
                break;
            }
        }
        return path;
    }
}
