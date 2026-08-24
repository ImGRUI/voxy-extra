package me.imgrui.storage;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Names Voxy's storage directory after the world itself instead of after the address the player connected to.
 * <p>
 * Voxy keys its LoD storage on {@code ServerData#ip}, so a server reachable through several addresses stores
 * one full copy of its LoD data per address and rebuilds everything from scratch whenever a player arrives
 * through a different one. The world's obfuscated biome seed, which the server sends in the login packet and
 * Voxy already keeps in {@code WorldIdentifier#biomeSeed}, identifies the world the same for every address,
 * every player and every session, so using it as the directory name makes those entrances share one cache.
 * <p>
 * All of this is a pure function of {@link LodStorageContext} and the seed; the caller observes the live
 * state and this class decides.
 */
public final class WorldSeedKey {

    /** Directory name prefix, shared verbatim with lod-server-support so the two can be compared by eye. */
    public static final String PREFIX = "world-";

    /**
     * The seed a server sends when it has no real world behind it. NanoLimbo style waiting rooms all send
     * this, and two unrelated waiting rooms sending it would otherwise pour their terrain into one directory.
     * <p>
     * Note that the old AntiSeedCracker and OsAntiSeedCracker defaults send a fixed {@code 69} rather than
     * zero; that is known and deliberately not special cased here.
     */
    public static final long PLACEHOLDER_SEED = 0L;

    private WorldSeedKey() {}

    /**
     * The directory name for a world, e.g. {@code world-00000000000000ff}. Negative seeds are rendered as
     * unsigned two's complement, so the name is always {@code world-} plus exactly 16 lowercase hex digits.
     */
    public static String directoryName(long biomeSeed) {
        return String.format(Locale.ROOT, PREFIX + "%016x", biomeSeed);
    }

    /**
     * Whether seed keying should be used at all. Every condition has to hold; when any one of them does not,
     * the caller keeps whatever path Voxy (and this mod's other features) already produced.
     */
    public static boolean appliesTo(LodStorageContext context, long biomeSeed) {
        return context.seedStorageEnabled()
                && context.remoteMultiplayer()
                && !context.replay()
                && biomeSeed != PLACEHOLDER_SEED;
    }

    /**
     * Swaps the address segment of Voxy's storage base path for the seed derived one, leaving the rest of the
     * path (and therefore the world identifier and storage segments Voxy appends afterwards) untouched.
     *
     * @return the seed keyed path, or {@code voxyBasePath} itself when seed keying does not apply
     */
    public static Path redirect(Path voxyBasePath, LodStorageContext context, long biomeSeed) {
        if (!appliesTo(context, biomeSeed)) return voxyBasePath;
        return voxyBasePath.resolveSibling(directoryName(biomeSeed));
    }
}
