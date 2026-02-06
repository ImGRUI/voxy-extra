package me.imgrui.mixin.flashback;

import com.moulberry.flashback.record.FlashbackMeta;
import com.moulberry.flashback.record.Recorder;
import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.flashback.IFlashbackMeta;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Recorder.class, remap = false)
public class FlashbackRecorderMixin {
    @Shadow private volatile boolean isPaused;
    @Shadow @Final private FlashbackMeta metadata;

    // SPDX-SnippetBegin
    // SPDX-FileCopyrightText: Copyright 2025 MCRcortex
    // SPDX-License-Identifier: ARR
    @Inject(method = "<init>", at = @At("TAIL"))
    private void voxyExtra$getPath(RegistryAccess registryAccess, CallbackInfo cif) {
        if (VoxyCommon.isAvailable()) {
            var voxyInstance = VoxyCommon.getInstance();
            if (voxyInstance instanceof VoxyClientInstance ci) {
                ((IFlashbackMeta)this.metadata).setLodPath(ci.getStorageBasePath().toFile());
            }
        }
    }
    // SPDX-SnippetEnd

    @Inject(method = "endTick", at = @At("TAIL"))
    private void voxyExtra$getDimensionChange(boolean close, CallbackInfo ci) {
        if (!isPaused) {
            Level level1 = Minecraft.getInstance().level;
            WorldIdentifier identifier1 = WorldIdentifier.of(level1);
            if (identifier1 != null) {
                FlashbackCopy.IDENTIFIERS.add(identifier1.getWorldId());
            }
        }
    }
}
