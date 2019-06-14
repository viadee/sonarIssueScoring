package de.viadee.sonarIssueScoring.service.prediction.load;

import org.junit.Assert;
import org.junit.Test;

public class GitPathTest {
    @Test public void testNormalization() {
        Assert.assertEquals(GitPath.of("home/sweet/home"), GitPath.of("home//sweet\\\\home"));
        Assert.assertNotEquals(GitPath.of("sweet/HOME/sweet"), GitPath.of("sweet/home/sweet"));
    }

    @Test public void filename() {
        Assert.assertEquals("file.txt", GitPath.of("some/file.txt").fileName());
    }

    @Test public void dir() {
        Assert.assertEquals(GitPath.of("some"), GitPath.of("some/file.txt").dir());
        Assert.assertEquals(GitPath.of(""), GitPath.of("some").dir());
        Assert.assertNull(GitPath.of("").dir());
    }

    @Test public void string() {
        Assert.assertEquals("some/file.txt", GitPath.of("some/file.txt").toString());
    }
}
