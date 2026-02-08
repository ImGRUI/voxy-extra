package me.imgrui.mixin.flashback;

import com.moulberry.flashback.screen.SaveReplayScreen;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SaveReplayScreen.class, remap = false)
public class FlashbackSaveReplayScreenMixin {
    @Inject(method = "deleteReplay()V", at = @At("TAIL"))
    private static void voxyExtra$deleteReplay(CallbackInfo ci) {
        if (VoxyExtraConfig.CONFIG.getSaveOldLods() && VoxyConfig.CONFIG.isRenderingEnabled()) FlashbackCopy.deleteReplayLOD();
    }
}
