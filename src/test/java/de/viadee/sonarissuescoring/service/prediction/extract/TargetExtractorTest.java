package de.viadee.sonarissuescoring.service.prediction.extract;

import static de.viadee.sonarissuescoring.service.prediction.load.Commit.DiffType.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;

import de.viadee.sonarissuescoring.service.prediction.load.Commit;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;

public class TargetExtractorTest {
    private static final GitPath pathA = GitPath.of("a"), pathB = GitPath.of("b"), pathC = GitPath.of("c"), pathD = GitPath.of("d");

    @Test public void createChangeHistogram() {
        List<Commit> commits = ImmutableList.of(//
                Commit.of("", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of(pathA, ADDED, pathB, ADDED), ImmutableMap.of()),//
                Commit.of("", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of(pathB, MODIFIED, pathC, MODIFIED, pathD, DELETED), ImmutableMap.of()));
        Assert.assertEquals(ImmutableMultiset.of(pathA, pathB, pathB, pathC, pathD), TargetExtractor.createChangeHistogram(commits));
    }

    @Test public void percentileChanging() {
        ImmutableMap<GitPath, Integer> counts = ImmutableMap.<GitPath, Integer>builder().
                put(GitPath.of("a"), 0).
                put(GitPath.of("b"), 0).
                put(GitPath.of("c"), 0).
                put(GitPath.of("d"), 0).
                put(GitPath.of("e"), 0).
                put(GitPath.of("f"), 0).
                put(GitPath.of("g"), 1).
                put(GitPath.of("h"), 1).
                put(GitPath.of("i"), 1).
                put(GitPath.of("j"), 1).
                put(GitPath.of("k"), 1).
                put(GitPath.of("l"), 2).
                put(GitPath.of("m"), 2).
                put(GitPath.of("n"), 777).build();

        Map<GitPath, Double> ranked = TargetExtractor.rank(counts);

        Assert.assertEquals(14, ranked.size());

        Assert.assertEquals(0, ranked.get(GitPath.of("a")), 1.0e-4);
        Assert.assertEquals(0.4286, ranked.get(GitPath.of("g")), 1.0e-4);
        Assert.assertEquals(0.7857, ranked.get(GitPath.of("l")), 1.0e-4);
        Assert.assertEquals(0.9286, ranked.get(GitPath.of("n")), 1.0e-4);
    }
}
