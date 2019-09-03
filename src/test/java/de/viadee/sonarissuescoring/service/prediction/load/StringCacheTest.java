package de.viadee.sonarissuescoring.service.prediction.load;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringCacheTest {

    @Test public void deduplicate() {
        StringCache cache = new StringCache();

        String a = "ASDF";
        String b = new String(a.getBytes());

        assertNotSame("Instances are already the same, update test to use different instances", a, b);

        assertSame(a, cache.deduplicate(a));
        assertSame(a, cache.deduplicate(b));
        cache.deduplicate(a);
    }
}
