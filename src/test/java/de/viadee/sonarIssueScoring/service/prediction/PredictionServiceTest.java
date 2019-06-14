package de.viadee.sonarIssueScoring.service.prediction;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

public class PredictionServiceTest {

    private EvaluationResultPath pair(double a, double b) {
        return EvaluationResultPath.of(GitPath.of(""), a, b);
    }

    @Test public void confusionMatrix() {
        List<EvaluationResultPath> values = new ArrayList<>(ImmutableList.of(//
                pair(0, 1),//
                pair(0, 1),//
                pair(1, 0),//
                pair(1, 0),//
                pair(1, 0),//
                pair(1, 1)));//

        for (int i = 0; i < 8; i++)
            values.add(pair(0, 0));

        Table<Boolean, Boolean, Integer> matrix = PredictionService.confusionMatrix(values);
        // actual, predicted
        Assert.assertEquals(8, (int) matrix.get(false, false));
        Assert.assertEquals(1, (int) matrix.get(true, true));
        Assert.assertEquals(3, (int) matrix.get(false, true));
        Assert.assertEquals(2, (int) matrix.get(true, false));
    }

    @Test public void rmse() {
        ImmutableList<EvaluationResultPath> values = ImmutableList.of(//
                pair(90, 80),// 10 * 10 = 100
                pair(50, 70),// 20 * 20 = 400
                pair(50, 50));// 0 *  0 =   0

        //sqrt(500/3)
        Assert.assertEquals(12.909, PredictionService.rmse(values), 1.0e-3);
    }

    @Test public void r2() {
        ImmutableList<EvaluationResultPath> values = ImmutableList.of(//
                pair(90, 80),//
                pair(76, 70),//
                pair(50, 0),//
                pair(33, 30),//
                pair(40, 40));

        Assert.assertEquals(0.58920, PredictionService.r2(values), 5.0e-5);
    }
}
