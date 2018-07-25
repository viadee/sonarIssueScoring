package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.JavaParser;

public class CyclomaticComplexityCalculatorTest {
    @Test
    public void testCyclomaticComplexity() {
        String source = "class A{" + // 0
                "void b(){ if(true||false){work();}}" + // method + if + ||  == 3
                "void d(){ try{a();}catch(IOE e){if(e==null){throw e;}}}}"; // method + catch + if ==3

        int cc = new CyclomaticComplexityCalculator().cyclomaticComplexity(JavaParser.parse(source));

        Assert.assertEquals(6, cc);
    }
}