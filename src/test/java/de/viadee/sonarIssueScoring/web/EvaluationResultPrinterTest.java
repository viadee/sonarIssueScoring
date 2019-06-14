package de.viadee.sonarIssueScoring.web;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.viadee.sonarIssueScoring.service.prediction.EvaluationResultPath;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

public class EvaluationResultPrinterTest {

    @Test public void formatPaths() {
        ImmutableList<EvaluationResultPath> paths = ImmutableList.of(//
                EvaluationResultPath.of(GitPath.of("01"), 2.0 / 3, 1.0 / 3),//
                EvaluationResultPath.of(GitPath.of("02"), -2.0 / 6, -1.0 / 6));

        String expected = "Predicted paths:\n";
        expected += " absError |  actual | predicted | path\n";
        expected += "   0.3333 |  0.3333 |    0.6667 | 01\n";
        expected += "   0.1667 | -0.1667 |   -0.3333 | 02\n\n";

        String actual = EvaluationResultPrinter.formatPaths(paths);

        Assert.assertEquals(expected, actual);
    }
}
