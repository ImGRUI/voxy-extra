package me.imgrui.mixin.flashback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moulberry.flashback.record.FlashbackMeta;
import com.moulberry.flashback.screen.EditReplayScreen;
import me.cortex.voxy.client.compat.IFlashbackMeta;
import me.cortex.voxy.client.config.VoxyConfig;
import me.imgrui.VoxyExtra;
import me.imgrui.flashback.CopiedLodPath;
import me.imgrui.flashback.FlashbackCopy;
import me.imgrui.flashback.SessionLodBases;
import me.imgrui.flashback.WarningTopic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static me.imgrui.VoxyExtra.mcPath;

/**
 * Records where this session's LoDs really are, in the one field a replay has for it.
 * <p>
 * The priority is load bearing: Voxy injects into the same method to write its own base path, and only by
 * running after it does the value written here survive. Should Voxy ever raise its own mixin above 1100 the
 * order flips and every rewrite below is silently overwritten - there is no cheap way to notice that at
 * runtime, so it is written down here instead. {@link me.imgrui.flashback.LodCopyPlan#voxyLooksSeedAware()}
 * watches the other half of the same question, Voxy reporting the seed keyed base by itself.
 */
@Mixin(value = FlashbackMeta.class, remap = false, priority = 1100)
public class FlashbackMetaMixin {
    @Shadow public UUID replayIdentifier;

    @Inject(method = "toJson", at = @At("RETURN"))
    private void voxyExtra$InjectLodPath(CallbackInfoReturnable<JsonObject> cir) {
        var Niko = cir.getReturnValue();
        if (Niko != null && ((IFlashbackMeta)this).getVoxyPath() != null && VoxyConfig.CONFIG.isRenderingEnabled()) {
            FlashbackCopy.replayIdentifier = replayIdentifier.toString();
            FlashbackCopy.basePath = ((IFlashbackMeta)this).getVoxyPath().toPath();
            Screen screen = Minecraft.getInstance().gui.screen();
            if (screen instanceof EditReplayScreen) {
                return;
            }
            if (VoxyExtra.CONFIG.flashbackSaveOldLods) {
                Path copyPath = mcPath.resolve(".voxy").resolve(CopiedLodPath.FLASHBACK_DIRECTORY).resolve(replayIdentifier.toString());
                Niko.addProperty(CopiedLodPath.STORAGE_PATH_KEY, copyPath.toString());
                return;
            }
            voxyExtra$recordSessionStoragePath(Niko);
        }
    }

    /**
     * Points the replay at where this session's LoDs really are.
     * <p>
     * Voxy records the base its client instance was built with, which is named after the server address and
     * is not where {@code worldSeedStorage} put anything. Rewriting the path here is enough on its own:
     * playback already reads the recorded path and takes it as the base, so nothing on the reading side has
     * to change and replays recorded before this still load exactly as they did.
     */
    @Unique
    private void voxyExtra$recordSessionStoragePath(JsonObject meta) {
        SessionLodBases session = FlashbackCopy.sessionLodBases();
        Optional<Path> soleBase = session.soleSeedBase();
        if (soleBase.isPresent()) {
            meta.addProperty(CopiedLodPath.STORAGE_PATH_KEY, soleBase.get().toString());
        } else if (session.seedKeyingApplied() && FlashbackCopy.shouldWarn(WarningTopic.MULTIPLE_SEED_BASES)) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] The worlds seen since connecting stored their LoDs under {} different world seeds, so no single path describes this replay; leaving the one Voxy recorded", session.seedBases().size());
        }

        Path recorded = voxyExtra$recordedStoragePath(meta);
        if (session.pointsOutsideSeedStorage(recorded) && FlashbackCopy.shouldWarn(WarningTopic.PATH_OUTSIDE_SEED_STORAGE)) {
            VoxyExtra.LOGGER.warn("[Voxy Extra] This replay records the LoD path {}, which is not where World Seed Storage put the LoDs of the worlds seen since connecting, so playing it back will not find them", recorded);
        }
    }

    /** What the replay ends up recording, read back rather than assumed, so the check above is honest. */
    @Unique
    private static Path voxyExtra$recordedStoragePath(JsonObject meta) {
        JsonElement recorded = meta.get(CopiedLodPath.STORAGE_PATH_KEY);
        if (recorded == null || !recorded.isJsonPrimitive()) return null;
        try {
            return Path.of(recorded.getAsString());
        } catch (InvalidPathException e) {
            return null;
        }
    }

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void voxyExtra$InjectGetLodPath(JsonObject meta, CallbackInfoReturnable<FlashbackMeta> cir) {
        var OneShot = cir.getReturnValue();
        if (OneShot != null && meta != null) {
            if (meta.has(CopiedLodPath.STORAGE_PATH_KEY)) {
                FlashbackCopy.voxySavedLods = meta.get(CopiedLodPath.STORAGE_PATH_KEY).toString().contains(OneShot.replayIdentifier.toString());
            } else {
                FlashbackCopy.voxySavedLods = false;
            }
        }
    }
}
