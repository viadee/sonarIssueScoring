package de.viadee.sonarIssueScoring.service.desirability;

import org.junit.Assert;
import org.junit.Test;

public class BaseServerInfoTest {
    @Test
    public void testToString() {
        Assert.assertEquals("ServerInfo[url=https://some.git.server, user=null, password==null]", ServerInfo.anonymous("https://some.git.server").toString());
        Assert.assertEquals("ServerInfo[url=https://some.git.server, user=one, password!=null]", ServerInfo.of("https://some.git.server", "one", "two").toString());
    }
}