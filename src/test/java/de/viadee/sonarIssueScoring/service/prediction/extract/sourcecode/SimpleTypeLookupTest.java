package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class SimpleTypeLookupTest {
    @Test
    public void testTypeLookup() {
        ParsedProject project = new ParsedProject(ImmutableMap.of( //
                Paths.get("PathA.java"), "class A{}", //
                Paths.get("PathA2.java"), "class A{}", //Intentional name collision with PathA
                Paths.get("PathB.java"), "class B{}"));

        SimpleTypeLookup lookup = new SimpleTypeLookup(project);

        //Two files define class A
        Assert.assertEquals(ImmutableSet.of(Paths.get("PathA.java"), Paths.get("PathA2.java")), lookup.getSourcePaths("A"));
        Assert.assertEquals(ImmutableSet.of(Paths.get("PathB.java")), lookup.getSourcePaths("B"));
    }
}