package me.imgrui.flashback;

/**
 * The things this mod warns about while a replay's metadata is being written, each said once.
 * <p>
 * Flashback rewrites that metadata every time it writes a replay chunk, so any warning decided there is
 * decided again every few seconds of recording. Naming the topics makes {@link FlashbackCopy#shouldWarn}
 * a closed question rather than a free string one.
 */
public enum WarningTopic {

    /** The recording crossed worlds with different seeds, so no single storage path describes it. */
    MULTIPLE_SEED_BASES,

    /** The replay ended up recording a storage path that is not where seed keying put the LoDs. */
    PATH_OUTSIDE_SEED_STORAGE
}
