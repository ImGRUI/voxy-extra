package me.imgrui.mixin.voxy;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.cortex.voxy.client.core.NormalRenderPipeline;
import me.imgrui.VoxyExtra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = NormalRenderPipeline.class,remap = false)
public class NormalRenderPipelineMixin {
    @ModifyExpressionValue(method = "finish", at = @At(value = "FIELD", target = "Lme/cortex/voxy/client/core/NormalRenderPipeline$FogMode;hasFog:Z", opcode = Opcodes.GETFIELD))
    public boolean voxyExtra$fixNetherFog(boolean original) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            return original && !(VoxyExtra.CONFIG.fixNetherFog && level.dimension().equals(Level.NETHER));
        }
        return original;
    }

    @WrapOperation(method = "finish", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 1))
    public float voxyExtra$changeFadeFogStart(float vanillaRd, float rd, Operation<Float> original) {
        if (VoxyExtra.CONFIG.customFogFade) {
            return original.call(vanillaRd, rd * 0.9F * VoxyExtra.CONFIG.fadeFogStart * 0.01F);
        }
        return original.call(vanillaRd, rd * 0.9F);
    }

    @WrapOperation(method = "finish", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 2))
    public float voxyExtra$changeFadeFogEnd(float vanillaRd, float rd, Operation<Float> original) {
        if (VoxyExtra.CONFIG.customFogFade) {
            return original.call(vanillaRd,rd * VoxyExtra.CONFIG.fadeFogEnd * 0.01F);
        }
        return original.call(vanillaRd, rd);
    }
}
