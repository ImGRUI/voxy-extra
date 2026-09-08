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
import me.imgrui.replay.ReplayCompat;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static me.imgrui.VoxyExtra.mcPath;

@Mixin(value = VoxyClientInstance.class, remap = false)
public class VoxyClientInstanceMixin {
    @Shadow
    @Final
    @Mutable
    private boolean noIngestOverride;

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void voxyExtra$replayIngest(CallbackInfo ci) {
        Path path = FlashbackCompat.getReplayStoragePath();
        ReplayHandler replayHandler = ReplayCompat.getReplayModHandler();
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
        ReplayHandler replayHandler = ReplayCompat.getReplayModHandler();
        if (replayHandler == null || !VoxyExtra.CONFIG.replayModLoadLods) {
            Logger.error("Server info null");
            return original.call(instance, other);
        }
        try {
            ReplayMetaData metaData = replayHandler.getReplayFile().getMetaData();
            String customServerName = metaData.getCustomServerName();
            String serverNameOrIp = metaData.getServerName();
            if (metaData.isSingleplayer()) {
                newBasePath = mcPath.resolve("saves").resolve(customServerName).resolve("voxy");
                VoxyExtra.LOGGER.info("[Voxy Extra] Loaded LoDs from {}", customServerName);
            }
            else if (Objects.equals(serverNameOrIp, "A Realms Server")) {
                newBasePath = basePath.resolve("realms");
                VoxyExtra.LOGGER.info("[Voxy Extra] Loaded LoDs from realms");
            }
            else {
                String serverNameOrIpReplaced = serverNameOrIp.replace(":", "_");
                newBasePath = basePath.resolve(serverNameOrIpReplaced);
                VoxyExtra.LOGGER.info("[Voxy Extra] Loaded LoDs from {}", serverNameOrIpReplaced);
            }
        } catch (IOException e) {
            VoxyExtra.LOGGER.error("[Voxy Extra] Failed to load Replay File");
        }
        if (!newBasePath.toFile().exists()) {
            newBasePath = original.call(instance, other);
            VoxyExtra.LOGGER.info("[Voxy Extra] Path to LoDs doesn't exist");
        }
        return newBasePath;
    }

    @WrapOperation(method = "getBasePath", at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;", ordinal = 6))
    private static Path voxyExtra$lodMirror(Path basePath, String serverAddress, Operation<Path> original) {
        return voxyExtra$lodMirrorCheck(original.call(basePath, serverAddress), serverAddress);
    }

    @Unique
    private static Path voxyExtra$lodMirrorCheck(Path path, String serverAddress) {
        if (!VoxyExtra.CONFIG.lodMirror) return path;
        if (VoxyExtra.CONFIG.lodMirrorList.isEmpty()) return path;
        var serverAddressNormalized = serverAddress.replace("_",":");
        for (int i = 0; i < VoxyExtra.CONFIG.lodMirrorList.size(); i++) {
            String[] list = VoxyExtra.CONFIG.lodMirrorList.get(i).trim().split("\\s+");
            var baseAddress = list[0];
            if (baseAddress.equals(serverAddressNormalized)) return path;
            if (ArrayUtils.contains(list, serverAddressNormalized)) {
                path = path.resolveSibling(baseAddress);
                VoxyExtra.LOGGER.warn("[Voxy Extra] Replaced path to {}", baseAddress);
                break;
            }
        }
        return path;
    }

    @WrapOperation(method = "getBasePath", at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;", ordinal = 6))
    private static Path voxyExtra$lodMerge(Path basePath, String serverAddress, Operation<Path> original) {
        return voxyExtra$lodMergeCheck(original.call(basePath, serverAddress), serverAddress);
    }

    @Unique
    private static Path voxyExtra$lodMergeCheck(Path path, String serverAddress) {
        if (!VoxyExtra.CONFIG.lodMerge) return path;
        if (VoxyExtra.CONFIG.lodMergeList.isEmpty()) return path;
        var serverAddressNormalized = serverAddress.replace("_",":");
        for (int i = 0; i < VoxyExtra.CONFIG.lodMergeList.size(); i++) {
            String baseAddress = VoxyExtra.CONFIG.lodMergeList.get(i);
            if (serverAddressNormalized.contains(baseAddress)) {
                path = path.resolveSibling(baseAddress);
                VoxyExtra.LOGGER.warn("[Voxy Extra] Replaced path to {}", baseAddress);
                break;
            }
        }
        return path;
    }
}
