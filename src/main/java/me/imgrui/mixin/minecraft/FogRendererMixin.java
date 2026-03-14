package me.imgrui.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;

import me.imgrui.config.VoxyExtraConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
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

@Mixin(value = FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow protected abstract FogType getFogType(Camera camera);

    @Inject(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", ordinal = 0, shift = At.Shift.AFTER))
    private void voxyExtra$modifyNetherFog(Camera camera, int i, DeltaTracker deltaTracker, float f, ClientLevel clientLevel, CallbackInfoReturnable<Vector4f> cir, @Local(type=FogData.class) FogData data) {
        FogType fogType = getFogType(camera);

        if (clientLevel != null && clientLevel.dimension() == Level.NETHER && fogType == FogType.ATMOSPHERIC) {
            var config = VoxyExtraConfig.CONFIG;

            if (!config.isNetherFogEnabled()) {
                data.environmentalStart = Float.MAX_VALUE;
                data.environmentalEnd = Float.MAX_VALUE;
                return;
            }

            float environmentStartMultiplier = config.netherFogStartMultiplier / 100.0F;
            float environmentEndMultiplier = config.netherFogEndMultiplier / 100.0F;

            data.environmentalStart *= environmentStartMultiplier;
            data.environmentalEnd *= environmentEndMultiplier;
        }
    }
}