package me.imgrui.replaymod;

import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import me.imgrui.VoxyExtra;

public class ReplayModCompat {
    public static ReplayHandler getReplayModHandler() {
        if (!VoxyExtra.isReplayModLoaded) {
            return null;
        }
        return ReplayModReplay.instance.getReplayHandler();
    }
}
