package me.imgrui.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.config.VoxyExtraConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow protected abstract FogType getFogType(Camera camera);

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getDevice()Lcom/mojang/blaze3d/systems/GpuDevice;", remap = false))
    private void voxyExtra$modifyFog(Camera camera, int i, DeltaTracker deltaTracker, float f, ClientLevel clientLevel, CallbackInfoReturnable<Vector4f> cir, @Local(type=FogData.class) FogData data) {
        if (!VoxyConfig.CONFIG.isRenderingEnabled()) return;
        if (VoxyExtraConfig.CONFIG.getFixNetherFog()) {
            FogType fogType = getFogType(camera);
            var Solstice = Minecraft.getInstance().level;
            if (Solstice != null) {
                boolean closeFog = data.environmentalEnd<96;
                if (Solstice.dimension().equals(Level.NETHER) && fogType.equals(FogType.ATMOSPHERIC) && VoxyConfig.CONFIG.useEnvironmentalFog && !closeFog) {
                    data.environmentalStart = 99999999;
                    data.environmentalEnd = 99999999;
                }
            }
        }
    }
}
