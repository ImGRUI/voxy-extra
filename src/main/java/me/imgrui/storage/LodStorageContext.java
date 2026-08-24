package me.imgrui.storage;

/**
 * Everything about the current client session that decides which directory Voxy's LoD data goes into.
 * <p>
 * The fields mirror, one for one, the state Voxy's own {@code VoxyClientInstance#getBasePath()} branches on,
 * plus the two replay mods this mod already integrates with. Keeping them as plain flags is what lets the
 * key selection in {@link WorldSeedKey} be a pure function that can be tested without Minecraft or Voxy.
 *
 * @param seedStorageEnabled the {@code worldSeedStorage} config option
 * @param localServer        an integrated server is running, so this is singleplayer (or a Flashback replay)
 * @param serverDataPresent  the client has the server entry Voxy names its directory after
 * @param realmsServer       that server entry is a Realm, which Voxy stores under {@code realms} instead
 * @param flashbackReplay    a Flashback replay is being played back
 * @param replayModReplay    a ReplayMod replay is being played back
 */
public record LodStorageContext(
        boolean seedStorageEnabled,
        boolean localServer,
        boolean serverDataPresent,
        boolean realmsServer,
        boolean flashbackReplay,
        boolean replayModReplay
) {
    /**
     * Whether this is a plain remote multiplayer session, i.e. exactly the case in which Voxy names the
     * storage directory after the address the player typed.
     */
    public boolean remoteMultiplayer() {
        return !this.localServer && this.serverDataPresent && !this.realmsServer;
    }

    /**
     * Whether a replay is being played back. Both replay mods override the storage base path to point at
     * recorded data, so seed keying must keep its hands off it or the replay reads from an empty directory.
     */
    public boolean replay() {
        return this.flashbackReplay || this.replayModReplay;
    }
}
