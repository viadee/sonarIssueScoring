package de.viadee.sonarIssueScoring.service.prediction.load;

import org.junit.Assert;
import org.junit.Test;

public class SnapshotStrategyTest {

    @Test
    public void generateOffsets() {
        Assert.assertArrayEquals(new int[]{0, 5, 10, 15, 20}, SnapshotStrategy.OVERLAP_ALWAYS.generateOffsets(10).limit(5).toArray());
        Assert.assertArrayEquals(new int[]{0, 10, 15, 20, 25}, SnapshotStrategy.NO_OVERLAP_ON_MOST_RECENT.generateOffsets(10).limit(5).toArray());
    }
}