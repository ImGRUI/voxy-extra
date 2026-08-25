package me.imgrui.flashback;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Carries out a {@link LodCopyPlan}.
 * <p>
 * Deliberately knows nothing about Minecraft, Voxy or this mod's own entry point: the plan says where to
 * read and write and the logger is handed in, so the copy can be exercised against a real directory tree
 * without a game around it.
 */
public final class LodCopier {

    /**
     * What must not be copied out of a live Voxy storage directory: the database's own log and lock files,
     * which belong to the running instance rather than to the terrain.
     */
    public static final FileFilter JOURNAL_FILTER =
            file -> !file.getName().contains("LOG") && !file.getName().equals("LOCK");

    private LodCopier() {}

    /**
     * @param copied  the worlds whose LoDs were copied
     * @param missing the worlds the plan pointed at a directory that does not exist - a replay recorded
     *                before Voxy ever built storage for that world, or LoDs deleted since
     */
    public record Report(List<String> copied, List<String> missing) {

        /**
         * Whether the copy produced nothing at all, so a caller does not announce a copy that never was.
         * A plan naming no world at all counts: there is still nothing in the replay's LoD directory.
         */
        public boolean copiedNothing() {
            return this.copied.isEmpty();
        }
    }

    /** Copies every world of {@code plan}, then Voxy's storage config, reporting rather than throwing. */
    public static Report copy(LodCopyPlan plan, FileFilter filter, Logger logger) {
        List<String> copied = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (LodCopyPlan.Entry entry : plan.entries()) {
            if (!Files.isDirectory(entry.source())) {
                missing.add(entry.worldId());
                logger.warn("[Voxy Extra] Found no LoDs to copy for world {}, nothing is stored in {}",
                        entry.worldId(), entry.source());
                continue;
            }
            try {
                FileUtils.copyDirectory(entry.source().toFile(), entry.destination().toFile(), filter);
                copied.add(entry.worldId());
            } catch (IOException e) {
                logger.error("[Voxy Extra] Failed to copy LoDs for world {}", entry.worldId(), e);
            }
        }

        try {
            FileUtils.copyFile(plan.configSource().toFile(), plan.configDestination().toFile());
        } catch (IOException e) {
            logger.error("[Voxy Extra] Failed to copy LoDs config.json", e);
        }

        return new Report(List.copyOf(copied), List.copyOf(missing));
    }
}
