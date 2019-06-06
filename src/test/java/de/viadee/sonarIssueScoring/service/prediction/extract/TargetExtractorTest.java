package de.viadee.sonarIssueScoring.service.prediction.extract;


import static de.viadee.sonarIssueScoring.service.prediction.load.Commit.DiffType.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;

public class TargetExtractorTest {
    private static final Path pathA = Paths.get("a"), pathB = Paths.get("b"), pathC = Paths.get("c"), pathD = Paths.get("d");

    @Test
    public void createChangeHistogram() {
        List<Commit> commits = ImmutableList.of(//
                Commit.of("", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of(pathA, ADDED, pathB, ADDED)),//
                Commit.of("", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of(pathB, MODIFIED, pathC, MODIFIED, pathD, DELETED)));
        Assert.assertEquals(ImmutableMultiset.of(pathA, pathB, pathB, pathC, pathD), TargetExtractor.createChangeHistogram(commits));
    }

    @Test
    public void percentileChanging() {
        ImmutableMap<Path, Integer> counts = ImmutableMap.<Path, Integer>builder().
                put(Paths.get("a"), 0).
                put(Paths.get("b"), 0).
                put(Paths.get("c"), 0).
                put(Paths.get("d"), 0).
                put(Paths.get("e"), 0).
                put(Paths.get("f"), 0).
                put(Paths.get("g"), 1).
                put(Paths.get("h"), 1).
                put(Paths.get("i"), 1).
                put(Paths.get("j"), 1).
                put(Paths.get("k"), 1).
                put(Paths.get("l"), 2).
                put(Paths.get("m"), 2).
                put(Paths.get("n"), 777).build();

        Map<Path, Double> ranked = TargetExtractor.rank(counts);

        Assert.assertEquals(14, ranked.size());

        Assert.assertEquals(0, ranked.get(Paths.get("a")), 1.0e-4);
        Assert.assertEquals(0.4286, ranked.get(Paths.get("g")), 1.0e-4);
        Assert.assertEquals(0.7857, ranked.get(Paths.get("l")), 1.0e-4);
        Assert.assertEquals(0.9286, ranked.get(Paths.get("n")), 1.0e-4);
    }
}