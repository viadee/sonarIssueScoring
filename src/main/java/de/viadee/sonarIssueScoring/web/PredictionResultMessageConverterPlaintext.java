package de.viadee.sonarIssueScoring.web;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.service.prediction.FileInformation;
import de.viadee.sonarIssueScoring.service.prediction.ModelMetrics;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;

@Component
public class PredictionResultMessageConverterPlaintext extends ToStringMessageConverter<PredictionResult> {

    public PredictionResultMessageConverterPlaintext() {
        super(PredictionResult.class);
    }

    @Override protected String write(PredictionResult res) {
        return formatValidationMetrics(res.validationMetrics()) +//
                StringTableFormatter.formatData("Variable Importances", "Variable", "Importance", res.validationMetrics().variableImportances(), true) +//
                formatResults(res.results());
    }

    private static String formatValidationMetrics(ModelMetrics m) {
        Map<String, Double> metrics = ImmutableMap.of("RMSE", m.rmse(), "R2", m.r2(), "MeanResidualDeviance", m.meanResidualDeviance());
        return StringTableFormatter.formatData("Validation Metrics", "Metric", "Value", metrics, false);
    }

    private static String formatResults(Map<Path, FileInformation> data) {
        ImmutableMap<String, Double> values = data.entrySet().stream().collect(
                ImmutableMap.toImmutableMap(e -> e.getKey().toString(), e -> e.getValue().predictedChangeCount()));
        return StringTableFormatter.formatData("Predictions", "File", "Predicted change", values, true);
    }
}
