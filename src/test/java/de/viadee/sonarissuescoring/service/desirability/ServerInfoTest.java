package de.viadee.sonarissuescoring.service.desirability;

import org.junit.Assert;
import org.junit.Test;

public class ServerInfoTest {
    @Test
    public void testToString() {
        Assert.assertEquals("ServerInfo{url=https://some.git.server}", ServerInfo.anonymous("https://some.git.server").toString());
        Assert.assertEquals("ServerInfo{url=https://some.git.server, user=one}", ServerInfo.of("https://some.git.server", "one", "two").toString());
    }
}
