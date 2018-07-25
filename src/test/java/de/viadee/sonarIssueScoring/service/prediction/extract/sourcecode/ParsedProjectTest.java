package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ParsedProjectTest {
    @Test
    public void testParse() {
        Map<Path, String> source = ImmutableMap.of(Paths.get("a/Primary.java"), "class Name{}", //Name is different from filename to check it was parsed successfully
                Paths.get("a/Secondary.java"), "this code is not valid java!"); //This should create a synthetic class for the filename

        ParsedProject parsed = new ParsedProject(source);

        assertTrue(parsed.get(Paths.get("a/Primary.java")).getClassByName("Name").isPresent());
        assertTrue(parsed.get(Paths.get("a/Secondary.java")).getClassByName("Secondary").isPresent()); //Fallback class should be present
        assertEquals(2, parsed.all().size());
    }
}