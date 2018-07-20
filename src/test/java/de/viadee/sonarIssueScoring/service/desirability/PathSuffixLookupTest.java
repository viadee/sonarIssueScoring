package de.viadee.sonarIssueScoring.service.desirability;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

public class PathSuffixLookupTest {

    @Test public void getOrDefault() {
        PathSuffixLookup<Double> lookup = new PathSuffixLookup<>(ImmutableMap.of(Paths.get("ide/is/superior"), 0.3, Paths.get("sata/is/bad"), 0.5));

        Assert.assertEquals(0.3, lookup.getOrDefault(Paths.get("is/superior"), Double.NaN), 1.0e-9);
        Assert.assertEquals(0.3, lookup.getOrDefault(Paths.get("ide/is/superior"), Double.NaN), 1.0e-9);
        Assert.assertEquals(0.5, lookup.getOrDefault(Paths.get("bad"), Double.NaN), 1.0e-9);
        Assert.assertEquals(Double.NaN, lookup.getOrDefault(Paths.get("cdrom"), Double.NaN), 1.0e-9);
    }
}