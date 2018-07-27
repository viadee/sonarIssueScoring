package de.viadee.sonarIssueScoring.service.prediction.load;

import java.util.stream.IntStream;

/**
 * Represents how to do the sampling at different times in the repository
 */
public enum SnapshotStrategy {
    OVERLAP_ALWAYS, //Snapshot ranges can overlap each other
    NO_OVERLAP_ON_MOST_RECENT; // Used for the evaluation, to get a complete, unseen future

    /**
     * @return The offsets from the current HEAD at which to create a snapshot
     */
    public IntStream generateOffsets(int predictionHorizon) {
        return IntStream.iterate(0, prev -> prev + predictionHorizon / 2).
                filter(i -> this == OVERLAP_ALWAYS || i == 0 || i >= predictionHorizon);
    }
}
