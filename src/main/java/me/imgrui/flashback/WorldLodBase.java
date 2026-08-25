package me.imgrui.flashback;

import java.nio.file.Path;

/**
 * Where one world of the current session actually stores its LoDs, as observed while Voxy built that
 * world's storage rather than derived a second time afterwards.
 * <p>
 * Voxy fixes one base path per client instance and hands that same path to Flashback, but this mod may
 * send an individual world somewhere else - {@code worldSeedStorage} names a world's directory after its
 * seed - and that decision is taken per world, after Flashback has already been told the old answer. This
 * record is what closes that gap: the copy of a replay's LoDs is taken from each world's own base.
 *
 * @param base      the directory Voxy's world identifier segment hangs off for this world
 * @param seedKeyed whether seed keying applied to this world, which is not the same as this mod having
 *                  moved it: should Voxy ever name the directory after the seed by itself the predicate
 *                  still holds while nothing moves, and that is precisely the case worth noticing
 */
public record WorldLodBase(Path base, boolean seedKeyed) {
}
