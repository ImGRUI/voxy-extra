package me.imgrui.flashback;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The seed keyed directories one recording session's LoDs ended up in.
 * <p>
 * A replay's metadata has room for a single storage path, and Voxy fills it in with the base its client
 * instance was built with - the one named after the server address - because that is the only path it knows
 * when the recording starts. When {@code worldSeedStorage} moved this session's worlds somewhere else, that
 * recorded path describes nothing the playback can use, so this class answers the one question the rewrite
 * needs: is there exactly one directory that stands for this session?
 * <p>
 * There is not always one. A session that crossed between worlds with different seeds used several, and no
 * single path describes it; the honest answer then is to leave Voxy's own value alone and say so.
 * <p>
 * Only worlds seed keying actually applied to count. A limbo waiting room sending the placeholder seed stays
 * under the address directory and must not be mistaken for a second seed.
 *
 * @param seedBases the distinct seed keyed bases the session used, absolute and normalised, in a
 *                  stable order
 */
public record SessionLodBases(List<Path> seedBases) {

    public static SessionLodBases of(Map<String, WorldLodBase> observedBases) {
        return new SessionLodBases(observedBases.values().stream()
                .filter(WorldLodBase::seedKeyed)
                .map(WorldLodBase::base)
                .map(SessionLodBases::canonical)
                .distinct()
                .sorted()
                .toList());
    }

    /**
     * The one shape every path in this class is held and compared in.
     * <p>
     * Voxy normalises its base path but does not make it absolute, while what gets written into a replay is
     * an absolute path; comparing the two as they come would call the successful case a mismatch whenever
     * the game directory is relative. Both sides go through here so the comparison is of like with like.
     */
    private static Path canonical(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /** Whether seed keying moved any world of this session at all. */
    public boolean seedKeyingApplied() {
        return !this.seedBases.isEmpty();
    }

    /** The one directory that stands for this session, or empty when several or none do. */
    public Optional<Path> soleSeedBase() {
        return this.seedBases.size() == 1 ? Optional.of(this.seedBases.getFirst()) : Optional.empty();
    }

    /**
     * Whether a replay that records {@code recordedPath} will look for this session's LoDs somewhere they
     * are not - the outcome worth warning about, checked against the path that was actually written rather
     * than against the branch that wrote it, so a rewrite that stops working still trips this.
     * <p>
     * {@code recordedPath} is brought into the same absolute, normalised shape the bases are held in, so a
     * relative game directory cannot make a successful rewrite look like a miss.
     */
    public boolean pointsOutsideSeedStorage(Path recordedPath) {
        return seedKeyingApplied() && (recordedPath == null || !this.seedBases.contains(canonical(recordedPath)));
    }
}
