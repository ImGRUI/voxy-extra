package me.imgrui.mixin.flashback;

import com.google.gson.JsonObject;
import com.moulberry.flashback.record.FlashbackMeta;
import com.moulberry.flashback.screen.EditReplayScreen;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.flashback.IFlashbackMeta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
public class FlashbackMetaMixin implements IFlashbackMeta {
    @Shadow public UUID replayIdentifier;
    @Unique private File lodPath;
    @Unique private boolean savedLods;

    @Override
    public void setLodPath(File lodPath) {
        this.lodPath = lodPath;
    }
    @Override
    public File getLodPath() {
        return this.lodPath;
    }
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
        var value = cir.getReturnValue();
        if (value != null && this.lodPath != null) {
            FlashbackCopy.replayIdentifier = replayIdentifier.toString();
            FlashbackCopy.basePath = getLodPath().toPath().toAbsolutePath();
            // Better check probably exists, but I don't know about it
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof EditReplayScreen) {
                if (getSavedLods()) {
                    Path copyPath = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy").resolve("flashback").resolve(replayIdentifier.toString()).toAbsolutePath();
                    value.addProperty("voxy_storage_path", copyPath.toString());
                } else {
                    value.addProperty("voxy_storage_path", getLodPath().getAbsoluteFile().getPath());
                }
                return;
            }
            if (VoxyExtraConfig.CONFIG.getSaveOldLods()) {
                Path copyPath = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy").resolve("flashback").resolve(replayIdentifier.toString()).toAbsolutePath();
                value.addProperty("voxy_storage_path", copyPath.toString());
            } else {
                value.addProperty("voxy_storage_path", getLodPath().getAbsoluteFile().getPath());
            }
        }
    }

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void voxyExtra$InjectGetLodPath(JsonObject meta, CallbackInfoReturnable<FlashbackMeta> cir) {
        var value = cir.getReturnValue();
        if (value != null && meta != null) {
            if (meta.has("voxy_storage_path")) {
                ((IFlashbackMeta)value).setSavedLods(meta.get("voxy_storage_path").toString().contains("voxy\\\\flashback\\\\"));
                FlashbackCopy.voxySavedLods = meta.get("voxy_storage_path").toString().contains("voxy\\\\flashback\\\\");
            } else {
                ((IFlashbackMeta)value).setSavedLods(false);
                FlashbackCopy.voxySavedLods = false;
            }
        }
    }
}
