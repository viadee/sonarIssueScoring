package de.viadee.sonarIssueScoring.service.prediction.extract;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.util.List;

import static de.viadee.sonarIssueScoring.service.prediction.load.BaseCommit.DiffType.*;

public class TargetExtractorTest {
    private static final Path pathA = Paths.get("a"), pathB = Paths.get("b"), pathC = Paths.get("c"), pathD = Paths.get("d");

    @Test public void createChangeHistogram() {
        List<Commit> commits = ImmutableList.of(//
                Commit.of("", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of(pathA, ADDED, pathB, ADDED)),//
                Commit.of("", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of(pathB, MODIFIED, pathC, MODIFIED, pathD, DELETED)));
        Assert.assertEquals(ImmutableMultiset.of(pathA, pathB, pathB, pathC, pathD), TargetExtractor.createChangeHistogram(commits));
    }
}