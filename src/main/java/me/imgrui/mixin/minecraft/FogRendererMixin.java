package me.imgrui.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.config.VoxyExtraConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow protected abstract FogType getFogType(Camera camera);

    @Redirect(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", opcode = Opcodes.PUTFIELD))
    private void voxyExtra$modifyfog(FogData instance, float distance, @Local(argsOnly = true) Camera camera) {
        if (VoxyConfig.CONFIG.isRenderingEnabled()) {
            FogType fogType = getFogType(camera);
            var level = Minecraft.getInstance().level;
            if (level != null) {
                if (level.dimension().equals(Level.NETHER) && fogType.equals(FogType.ATMOSPHERIC) && VoxyExtraConfig.CONFIG.getFixNetherFog() && VoxyConfig.CONFIG.useEnvironmentalFog) {
                    instance.environmentalStart = 1;
                    instance.environmentalEnd = 99999999;
                }
            }
        }
    }
}
