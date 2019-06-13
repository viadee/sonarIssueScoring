package de.viadee.sonarIssueScoring.web;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.viadee.sonarIssueScoring.service.prediction.EvaluationResultPath;

public class EvaluationResultPrinterTest {

    @Test public void formatPaths() {
        ImmutableList<EvaluationResultPath> paths = ImmutableList.of(//
                EvaluationResultPath.of(Paths.get("01"), 2.0 / 3, 1.0 / 3),//
                EvaluationResultPath.of(Paths.get("02"), -2.0 / 6, -1.0 / 6));

        String expected = "Predicted paths:\n";
        expected += " absError |  actual | predicted | path\n";
        expected += "   0.3333 |  0.3333 |    0.6667 | 01\n";
        expected += "   0.1667 | -0.1667 |   -0.3333 | 02\n\n";

        String actual = EvaluationResultPrinter.formatPaths(paths);

        Assert.assertEquals(expected, actual);
    }
}
