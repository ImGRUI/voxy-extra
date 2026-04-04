package me.imgrui.mixin.flashback;

import com.moulberry.flashback.record.Recorder;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import me.imgrui.flashback.FlashbackCopy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Recorder.class, remap = false)
public class FlashbackRecorderMixin {
    @Shadow private volatile boolean isPaused;
    @Unique private WorldIdentifier currentIdentifier;

    @Inject(method = "endTick", at = @At("TAIL"))
    private void voxyExtra$getDimensionChange(boolean close, CallbackInfo ci) {
        if (!isPaused) {
            Level level = Minecraft.getInstance().level;
            WorldIdentifier identifier = WorldIdentifier.of(level);
            if (identifier != null && identifier != currentIdentifier) {
                FlashbackCopy.IDENTIFIERS.add(identifier.getWorldId());
                currentIdentifier = identifier;
            }
        }
    }
}
