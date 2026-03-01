package me.imgrui.mixin.minecraft;

import me.imgrui.VoxyExtra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    public abstract ServerData getServerData();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void voxyExtra$captureHost(
            Minecraft minecraft,
            Connection connection,
            CommonListenerCookie cookie,
            CallbackInfo ci
    ) {
        ServerData data = this.getServerData();

        if (data == null || data.ip.isBlank()) {
            VoxyExtra.HOST = null;
            return;
        }

        VoxyExtra.HOST = ServerAddress.parseString(data.ip).getHost();
    }
}