package me.imgrui.mixin.flashback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moulberry.flashback.record.FlashbackMeta;
import com.moulberry.flashback.screen.EditReplayScreen;
import me.cortex.voxy.client.compat.IFlashbackMeta;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.flashback.IFlashbackM;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@Mixin(value = FlashbackMeta.class, remap = false, priority = 1100)
public class FlashbackMetaMixin implements IFlashbackM {
    @Shadow public UUID replayIdentifier;
    @Unique private boolean savedLods;

    @Override
    public void setSavedLods(boolean value) {
        this.savedLods = value;
    }

    @Override
    public boolean getSavedLods() {
        return this.savedLods;
    }

    @Inject(method = "toJson", at = @At("RETURN"))
    private void voxyExtra$InjectLodPath(CallbackInfoReturnable<JsonObject> cir) {
        JsonObject meta = cir.getReturnValue();
        IFlashbackMeta flashbackMeta = (IFlashbackMeta) this;
        File voxyPath = flashbackMeta.getVoxyPath();

        if (meta != null && voxyPath != null && VoxyConfig.CONFIG.isRenderingEnabled()) {
            Minecraft instance = Minecraft.getInstance();

            FlashbackCopy.replayIdentifier = replayIdentifier.toString();
            FlashbackCopy.basePath = voxyPath.toPath();

            Path copyPath = instance.gameDirectory
                .toPath()
                .resolve(".voxy")
                .resolve("flashback")
                .resolve(replayIdentifier.toString());
 
            if (instance.screen instanceof EditReplayScreen) {
                if (this.getSavedLods()) {
                    meta.addProperty("voxy_storage_path", copyPath.toString());
                }

                return;
            }

            if (VoxyExtraConfig.CONFIG.getFlashbackIngest()) {
                meta.addProperty("voxy_storage_path", copyPath.toString());
            }
        }
    }

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void voxyExtra$InjectGetLodPath(JsonObject meta, CallbackInfoReturnable<FlashbackMeta> cir) {
        FlashbackMeta flashbackMeta = cir.getReturnValue();
        boolean isSaved = false;
        JsonElement pathElement = meta.get("voxy_storage_path");

        if (flashbackMeta != null && pathElement != null) {
            String pathStr = pathElement.getAsString();
            isSaved = FlashbackCopy.getVoxyFlashbackPath(pathStr);
        }

        if (flashbackMeta instanceof IFlashbackM savedLodsMeta) {
            savedLodsMeta.setSavedLods(isSaved);
            FlashbackCopy.voxySavedLods = isSaved;
        }
    }
}
