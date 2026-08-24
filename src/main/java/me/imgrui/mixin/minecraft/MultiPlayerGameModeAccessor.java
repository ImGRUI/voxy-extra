package me.imgrui.mixin.minecraft;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the connection Voxy itself reads when it decides which directory to store LoDs in.
 * <p>
 * {@code Minecraft#getConnection()} is not equivalent: it goes through the local player, which is created
 * later than the game mode, so it can still be null when the first world engine asks for its storage.
 */
@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
    @Accessor("connection")
    ClientPacketListener voxyExtra$connection();
}
