package me.imgrui.mixin.minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogEnvironment.class)
public class FogEnvironmentMixin {
    @Inject(method = "sodium_extra$applyFogSettings", at = @At("HEAD"), cancellable = true, remap = false)
    private void voxyExtra$blockSodiumExtraInNether(FogType fogType, FogData fogData, Entity entity, BlockPos blockPos, ClientLevel level, float viewDistance, CallbackInfo ci) {
        if (level != null && level.dimension() == Level.NETHER && fogType == FogType.ATMOSPHERIC) {
            ci.cancel();
        }
    }
}