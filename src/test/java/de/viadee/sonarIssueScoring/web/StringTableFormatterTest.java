package de.viadee.sonarIssueScoring.web;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class StringTableFormatterTest {
    @Test public void testFormatting() {
        ImmutableMap<String, Double> data = ImmutableMap.of("aa", 2.0d, "bbbb", 0.5);
        String formatted = StringTableFormatter.formatData("Title", "Keys", "Values", data, true);
        String expected = "Title:\nKeys | Values\n-----+-------\n  aa | 2.000\nbbbb | 0.500\n\n";
        Assert.assertEquals(expected, formatted);
    }
}