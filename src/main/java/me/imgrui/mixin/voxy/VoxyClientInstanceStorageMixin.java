package me.imgrui.mixin.voxy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.compat.FlashbackCompat;
import me.cortex.voxy.common.config.ConfigBuildCtx;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import me.imgrui.VoxyExtra;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.mixin.minecraft.MultiPlayerGameModeAccessor;
import me.imgrui.replay.ReplayCompat;
import me.imgrui.storage.LodStorageContext;
import me.imgrui.storage.WorldSeedKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/**
 * Lets several entrances to one world share a single LoD cache, by naming the storage directory after the
 * world's biome seed rather than after the address the player connected to.
 * <p>
 * The interception sits on {@code createStorage} rather than on the base path the instance is built with,
 * because Voxy builds one client instance per session but one world engine per world: only here is the
 * world (and therefore its seed) known, and only here does each world of a session get its own answer.
 * <p>
 * That also makes this the one place where each world's real storage base is known without deriving it a
 * second time, so it is recorded here for {@link FlashbackCopy}: Voxy tells Flashback the instance wide base
 * it was built with, which is not where a world this class moved actually ended up.
 */
@Mixin(value = VoxyClientInstance.class, remap = false)
public class VoxyClientInstanceStorageMixin {

    @Unique
    private static boolean voxyExtra$warnedAboutStorageShape;

    /**
     * A new client instance means a new base path, so whatever was observed about the last one is stale.
     * Recording ends do not clear it; only a new instance does.
     * <p>
     * Order against {@code VoxyClientInstanceMixin}'s injection at this same point is not guaranteed and
     * does not need to be: neither of the two touches anything the other reads.
     */
    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void voxyExtra$resetWorldBases(CallbackInfo ci) {
        FlashbackCopy.forgetWorldBases();
    }

    @WrapOperation(
            method = "createStorage",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/cortex/voxy/common/config/ConfigBuildCtx;setProperty(Ljava/lang/String;Ljava/lang/String;)Lme/cortex/voxy/common/config/ConfigBuildCtx;",
                    ordinal = 0
            )
    )
    private ConfigBuildCtx voxyExtra$worldSeedBasePath(ConfigBuildCtx ctx, String property, String value,
                                                       Operation<ConfigBuildCtx> original,
                                                       @Local(argsOnly = true) WorldIdentifier identifier) {
        LodStorageContext context = voxyExtra$storageContext();

        // Voxy sets the base save path first; if it ever stops doing so this wrapper silently becomes a no-op,
        // because require=1 only checks that the injection point exists, not that it still means what it meant.
        if (!ConfigBuildCtx.BASE_SAVE_PATH.equals(property)) {
            if (context.seedStorageEnabled() && !voxyExtra$warnedAboutStorageShape) {
                voxyExtra$warnedAboutStorageShape = true;
                VoxyExtra.LOGGER.warn("[Voxy Extra] Voxy no longer builds its storage path by setting {} first, so World Seed Storage is not taking effect; LoDs stay stored by server address", ConfigBuildCtx.BASE_SAVE_PATH);
            }
            return original.call(ctx, property, value);
        }

        Path basePath = Path.of(value);
        boolean seedKeyed = WorldSeedKey.appliesTo(context, identifier.biomeSeed);
        Path redirected = WorldSeedKey.redirect(basePath, context, identifier.biomeSeed);
        FlashbackCopy.rememberWorldBase(identifier.getWorldId(), redirected, seedKeyed);
        if (redirected.equals(basePath)) return original.call(ctx, property, value);

        VoxyExtra.LOGGER.info("[Voxy Extra] Storing LoDs by world seed in {}", redirected.getFileName());
        return original.call(ctx, property, redirected.toString());
    }

    @Unique
    private static LodStorageContext voxyExtra$storageContext() {
        Minecraft minecraft = Minecraft.getInstance();
        ServerData serverData = voxyExtra$serverData(minecraft);
        return new LodStorageContext(
                VoxyExtra.CONFIG != null && VoxyExtra.CONFIG.worldSeedStorage,
                minecraft.getSingleplayerServer() != null,
                serverData != null,
                serverData != null && serverData.isRealm(),
                FlashbackCompat.getReplayStoragePath() != null,
                ReplayCompat.getReplayModHandler() != null
        );
    }

    @Unique
    private static ServerData voxyExtra$serverData(Minecraft minecraft) {
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (gameMode == null) return null;
        ClientPacketListener connection = ((MultiPlayerGameModeAccessor) gameMode).voxyExtra$connection();
        return connection == null ? null : connection.getServerData();
    }
}
