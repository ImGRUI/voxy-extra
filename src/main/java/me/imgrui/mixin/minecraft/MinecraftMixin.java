package me.imgrui.mixin.minecraft;

import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.imgrui.VoxyExtra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public class MinecraftMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    public void voxyExtra$handleBlacklist(Screen screen, boolean bl, boolean bl2, CallbackInfo ci) {
        if (VoxyExtra.isInBlacklist && !VoxyConfig.CONFIG.enabled) {
            VoxyConfig.CONFIG.enabled = true;
            IrisUtil.reload();
        }
        VoxyExtra.isInBlacklist = false;
    }
}
