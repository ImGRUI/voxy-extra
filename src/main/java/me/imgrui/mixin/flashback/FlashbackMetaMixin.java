package me.imgrui.mixin.flashback;

import com.google.gson.JsonObject;
import com.moulberry.flashback.record.FlashbackMeta;
import com.moulberry.flashback.screen.EditReplayScreen;
import me.cortex.voxy.client.compat.IFlashbackMeta;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.flashback.IFlashbackM;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.UUID;

@Mixin(value = FlashbackMeta.class, remap = false, priority = 1100)
public class FlashbackMetaMixin implements IFlashbackM {
    @Shadow public UUID replayIdentifier;
    @Unique private boolean savedLods;

    @Override
    public void setSavedLods(boolean savedLods) {
        this.savedLods = savedLods;
    }
    @Override
    public boolean getSavedLods() {
        return this.savedLods;
    }

    @Inject(method = "toJson", at = @At("RETURN"))
    private void voxyExtra$InjectLodPath(CallbackInfoReturnable<JsonObject> cir) {
        var Niko = cir.getReturnValue();
        if (Niko != null && ((IFlashbackMeta)this).getVoxyPath() != null) {
            FlashbackCopy.replayIdentifier = replayIdentifier.toString();
            FlashbackCopy.basePath = ((IFlashbackMeta)this).getVoxyPath().toPath();
            // Better check probably exists, but I don't know about it
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof EditReplayScreen) {
                if (getSavedLods()) {
                    Path copyPath = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy").resolve("flashback").resolve(replayIdentifier.toString());
                    Niko.addProperty("voxy_storage_path", copyPath.toString());
                }
                return;
            }
            if (VoxyExtraConfig.CONFIG.getSaveOldLods()) {
                Path copyPath = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy").resolve("flashback").resolve(replayIdentifier.toString());
                Niko.addProperty("voxy_storage_path", copyPath.toString());
            }
        }
    }

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void voxyExtra$InjectGetLodPath(JsonObject meta, CallbackInfoReturnable<FlashbackMeta> cir) {
        var OneShot = cir.getReturnValue();
        if (OneShot != null && meta != null) {
            if (meta.has("voxy_storage_path")) {
                boolean saved = meta.get("voxy_storage_path").toString().contains("voxy\\\\flashback\\\\");
                ((IFlashbackM)OneShot).setSavedLods(saved);
                FlashbackCopy.voxySavedLods = saved;
            } else {
                ((IFlashbackM)OneShot).setSavedLods(false);
                FlashbackCopy.voxySavedLods = false;
            }
        }
    }
}
