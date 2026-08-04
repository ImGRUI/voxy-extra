package me.imgrui.mixin.replaymod;

import com.llamalad7.mixinextras.sugar.Local;
import com.replaymod.recording.handler.ConnectionEventHandler;
import me.imgrui.VoxyExtra;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ConnectionEventHandler.class, remap = false)
public class ConnectionEventHandlerMixin {
    @Shadow
    @Final
    private static Minecraft mc;

    @ModifyArg(method = "onConnectedToServerEvent", at = @At(value = "INVOKE", target = "Lcom/replaymod/replaystudio/replay/ReplayMetaData;setCustomServerName(Ljava/lang/String;)V"))
    private String voxyExtra$changeCustomServerNameToLevelPath(String customServerName, @Local(name = "local") boolean local) {
        if (local && mc.getSingleplayerServer() != null && VoxyExtra.CONFIG.replayModSaveCustomMeta) {
            return mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
        }
        return customServerName;
    }
}
