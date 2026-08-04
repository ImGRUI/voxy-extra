package me.imgrui.replay;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.playback.ReplayServer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import me.imgrui.VoxyExtra;

public class ReplayCompat {
    public static ReplayHandler getReplayModHandler() {
        if (!VoxyExtra.isReplayModLoaded) {
            return null;
        }
        return ReplayModReplay.instance.getReplayHandler();
    }

    public static ReplayServer getFlashbackReplayServer() {
        if (!VoxyExtra.isFlashbackLoaded) {
            return null;
        }
        return Flashback.getReplayServer();
    }
}
