package de.viadee.sonarIssueScoring.service.desirability;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

public class PathSuffixLookupTest {

    @Test public void getOrDefault() {
        PathSuffixLookup<Double> lookup = new PathSuffixLookup<>(ImmutableMap.of(GitPath.of("ide/is/superior"), 0.3, GitPath.of("sata/is/bad"), 0.5));

        Assert.assertEquals(0.3, lookup.getOrDefault(GitPath.of("is/superior"), Double.NaN), 1.0e-9);
        Assert.assertEquals(0.3, lookup.getOrDefault(GitPath.of("ide/is/superior"), Double.NaN), 1.0e-9);
        Assert.assertEquals(0.5, lookup.getOrDefault(GitPath.of("bad"), Double.NaN), 1.0e-9);
        Assert.assertEquals(Double.NaN, lookup.getOrDefault(GitPath.of("cdrom"), Double.NaN), 1.0e-9);
    }
}
