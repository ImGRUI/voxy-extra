package me.imgrui.mixin.voxy;

import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.compat.FlashbackCompat;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(value = VoxyClientInstance.class, remap = false)
public class VoxyClientInstanceMixin {
    @Shadow
    @Final
    @Mutable
    private boolean noIngestOverride;

    @Redirect(method = "<init>()V", at = @At(value = "FIELD", target = "Lme/cortex/voxy/client/VoxyClientInstance;noIngestOverride:Z", opcode = Opcodes.PUTFIELD))
    private void voxyExtra$redirectIngest(VoxyClientInstance instance, boolean value) {
        Path path = FlashbackCompat.getReplayStoragePath();
        if (VoxyExtraConfig.CONFIG.getFlashbackIngest()) {
            this.noIngestOverride = path != null && !FlashbackCopy.voxySavedLods;
        } else {
            this.noIngestOverride = path != null;
        }
    }
}
