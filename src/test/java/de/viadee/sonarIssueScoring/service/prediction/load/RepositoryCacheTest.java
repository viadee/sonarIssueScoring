package de.viadee.sonarIssueScoring.service.prediction.load;

import org.junit.Assert;
import org.junit.Test;

public class RepositoryCacheTest {
    @Test public void extractRepositoryName() {
        Assert.assertEquals("repo-google.guava.2f3d28dec6073c346afa1061c8292c49", RepositoryCache.extractRepositoryName("https://github.com/google/guava"));
    }
}