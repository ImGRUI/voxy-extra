package me.imgrui.flashback;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Which directories a replay's LoDs have to be copied from and to.
 * <p>
 * Flashback records one storage path per replay, the one Voxy's client instance was built with, so a world
 * this mod moved elsewhere is not under it. The plan therefore resolves every world against its own
 * observed {@link WorldLodBase} and falls back to the recorded path only for worlds nothing was seen of,
 * which is exactly what the previous single-base copy did for every world.
 * <p>
 * {@code config.json} is deliberately not treated that way: Voxy writes it once per client instance next to
 * the recorded base, never inside a seed keyed directory, so the recorded base is the only place it exists.
 * <p>
 * All of this is a pure function of the observed state; the caller observes and this class decides.
 */
public record LodCopyPlan(List<Entry> entries, Path configSource, Path configDestination,
                          List<String> seedAwareWorlds) {

    /** Voxy's per client instance storage config, which lives beside the base path rather than in a world. */
    public static final String CONFIG_FILE = "config.json";

    /**
     * One world's copy, from wherever it really lives to its place inside this replay's LoD directory.
     */
    public record Entry(String worldId, Path source, Path destination) {
    }

    /**
     * @param worldIds      the worlds this replay visited
     * @param observedBases where each world of the session was observed to actually store its LoDs
     * @param recordedBase  the single base path Flashback recorded in the replay's metadata
     * @param destination   this replay's LoD directory, {@code .voxy/flashback/<replay uuid>}
     */
    public static LodCopyPlan of(Collection<String> worldIds,
                                 Map<String, WorldLodBase> observedBases,
                                 Path recordedBase,
                                 Path destination) {
        List<Entry> entries = new ArrayList<>();
        List<String> seedAware = new ArrayList<>();
        // Sorted, so a plan is worth comparing against another plan and a log line reads the same twice.
        for (String worldId : new TreeSet<>(worldIds)) {
            WorldLodBase observed = observedBases.get(worldId);
            Path base = observed == null ? recordedBase : observed.base();
            if (observed != null && observed.seedKeyed() && base.equals(recordedBase)) {
                seedAware.add(worldId);
            }
            entries.add(new Entry(worldId, base.resolve(worldId), destination.resolve(worldId)));
        }
        return new LodCopyPlan(List.copyOf(entries),
                recordedBase.resolve(CONFIG_FILE),
                destination.resolve(CONFIG_FILE),
                List.copyOf(seedAware));
    }

    /**
     * Whether Voxy has started recording the seed keyed path itself.
     * <p>
     * A world this mod moved cannot also be where Voxy said it was, so the two paths agreeing means Voxy
     * now reports the moved location - at which point this mod's redirect is doing nothing and the copy is
     * one Voxy release away from being taken from the wrong place. Worth saying out loud.
     */
    public boolean voxyLooksSeedAware() {
        return !this.seedAwareWorlds.isEmpty();
    }
}
