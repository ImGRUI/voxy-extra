package me.imgrui.mixin.voxy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.compat.FlashbackCompat;
import me.cortex.voxy.common.Logger;
import me.imgrui.VoxyExtra;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.replaymod.ReplayModCompat;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mixin(value = VoxyClientInstance.class, remap = false)
public class VoxyClientInstanceMixin {
    @Shadow
    @Final
    @Mutable
    private boolean noIngestOverride;

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void voxyExtra$replayIngest(CallbackInfo ci) {
        Path path = FlashbackCompat.getReplayStoragePath();
        ReplayHandler replayHandler = ReplayModCompat.getReplayModHandler();
        if (replayHandler != null) {
            this.noIngestOverride = true;
            return;
        }
        if (path != null && VoxyExtra.CONFIG.flashbackIngest) {
            this.noIngestOverride = !FlashbackCopy.voxySavedLods;
        }
    }

    // ReplayMod
    @Redirect(method = "getBasePath", at = @At(value = "INVOKE", target = "Lme/cortex/voxy/common/Logger;error([Ljava/lang/Object;)V", ordinal = 1))
    private static void voxyExtra$cancelReplayModError(Object[] args){}
    @WrapOperation(method = "getBasePath", at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;", ordinal = 4))
    private static Path voxyExtra$getReplayModPath(Path instance, String other, Operation<Path> original, @Local(name = "basePath") Path basePath) {
        Path newBasePath = original.call(instance, other);
        ReplayHandler replayHandler = ReplayModCompat.getReplayModHandler();
        if (replayHandler == null) {
            // Means we are not in a replay "server", and the error is caused by something else.
            Logger.error("Server info null");
            return original.call(instance, other);
        }
        try {
            ReplayMetaData metaData = replayHandler.getReplayFile().getMetaData();
            String customServerName = metaData.getCustomServerName();
            String serverNameOrIp = metaData.getServerName();
            if (metaData.isSingleplayer()) {
                newBasePath = Minecraft.getInstance().gameDirectory.toPath().resolve("saves").resolve(customServerName).resolve("voxy");
            }
            else if (Objects.equals(serverNameOrIp, "A Realms Server")) {
                newBasePath = basePath.resolve("realms");
            }
            else {
                newBasePath = basePath.resolve(serverNameOrIp.replace(":", "_"));
            }
        } catch (IOException e) {
            VoxyExtra.LOGGER.error("[Voxy Extra] Failed to load Replay File");
        }
        if (!newBasePath.toFile().exists()) {
            newBasePath = original.call(instance, other);
            Logger.error("Server info null");
        }
        return newBasePath;
    }

    @ModifyVariable(method = "<init>()V", at = @At(value = "INVOKE_ASSIGN", target = "Lme/cortex/voxy/client/VoxyClientInstance;getBasePath()Ljava/nio/file/Path;"), name = "path")
    private static Path voxyExtra$lodMirror(Path path) {
        return voxyExtra$lodMirrorCheck(path);
    }

    @Unique
    private static Path voxyExtra$lodMirrorCheck(Path path) {
        if (!VoxyExtra.CONFIG.lodMirror) return path;
        var IP = VoxyExtra.IP;
        if (IP == null) return path;
        if (VoxyExtra.CONFIG.lodMirrorList.isEmpty()) return path;
        for (int i = 0; i < VoxyExtra.CONFIG.lodMirrorList.size(); i++) {
            String[] list = VoxyExtra.CONFIG.lodMirrorList.get(i).trim().split("\\s+");
            var listFirst = list[0];
            if (listFirst.equals(IP)) return path;
            if (ArrayUtils.contains(list,IP)) {
                path = path.resolveSibling(listFirst);
                VoxyExtra.LOGGER.warn("[Voxy Extra] Replaced path to {}", listFirst);
                break;
            }
        }
        return path;
    }
}
