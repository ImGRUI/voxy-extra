package me.imgrui.mixin.flashback;

import com.moulberry.flashback.Flashback;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Flashback.class, remap = false)
public class FlashbackMixin {
    @Inject(method = "finishRecordingReplay()V", at = @At("TAIL"))
    private static void voxyExtra$copylods(CallbackInfo ci) {
        if (!VoxyConfig.CONFIG.isRenderingEnabled()) return;
        if (VoxyExtraConfig.CONFIG.getSaveOldLods()) FlashbackCopy.CopyLods();
        FlashbackCopy.IDENTIFIERS.clear();
    }

    @Inject(method = "cancelRecordingReplay()V", at = @At("TAIL"))
    private static void voxyExtra$deleteIdentifiers(CallbackInfo ci) {
        FlashbackCopy.IDENTIFIERS.clear();
    }
}
