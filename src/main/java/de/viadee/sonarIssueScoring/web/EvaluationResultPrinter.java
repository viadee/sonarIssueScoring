package de.viadee.sonarIssueScoring.web;

import static de.viadee.sonarIssueScoring.web.StringTableFormatter.formatData;
import static de.viadee.sonarIssueScoring.web.StringTableFormatter.formatModelMetrics;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import de.viadee.sonarIssueScoring.service.prediction.EvaluationResult;
import de.viadee.sonarIssueScoring.service.prediction.EvaluationResultPath;

public class EvaluationResultPrinter {
    public static String asString(EvaluationResult result) {
        return formatModelMetrics("Validation", result.validationMetrics()) +//
                formatData("Actual metrics", "Metric", "Value", ImmutableMap.of("RMSE", result.actualRmse(), "R2", result.actualR2()), false) +//
                confusionMatrix(result) + formatPaths(result.evaluatedPaths());
    }

    private static String confusionMatrix(EvaluationResult in) {
        String out = "Confusion matrix: \n";
        out += "                      predicted major changes    predicted minor changes\n";
        out += "real major changes    " + cell(in, true, true) + "    " + cell(in, true, false) + "\n";
        out += "real minor changes    " + cell(in, false, true) + "    " + cell(in, false, false) + "\n\n";
        return out;
    }

    private static String cell(EvaluationResult res, boolean actual, boolean predicted) {
        return Strings.padStart(res.confusionMatrix().get(actual, predicted) + "", 23, ' ');
    }

    @VisibleForTesting static String formatPaths(List<EvaluationResultPath> paths) {
        String out = "Predicted paths:\n";

        out += " absError |  actual | predicted | path\n";
        for (EvaluationResultPath path : Ordering.from(Comparator.comparing(EvaluationResultPath::absError).reversed()).sortedCopy(paths)) {
            out += String.format(Locale.US, "  % 6.4f | % 6.4f |   % 6.4f | %s\n", path.absError(), path.actual(), path.predicted(), path.path());
        }

        return out+"\n";
    }
}
