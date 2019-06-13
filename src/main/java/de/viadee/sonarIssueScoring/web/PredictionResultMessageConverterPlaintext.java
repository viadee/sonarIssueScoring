package de.viadee.sonarIssueScoring.web;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.service.prediction.FileInformation;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;

@Component
public class PredictionResultMessageConverterPlaintext extends ToStringMessageConverter<PredictionResult> {

    public PredictionResultMessageConverterPlaintext() {
        super(PredictionResult.class);
    }

    @Override protected String write(PredictionResult res) {
        return StringTableFormatter.formatModelMetrics("Validation", res.validationMetrics()) + formatResults(res.results());
    }

    private static String formatResults(Map<Path, FileInformation> data) {
        ImmutableMap<String, Double> values = data.entrySet().stream().collect(
                ImmutableMap.toImmutableMap(e -> e.getKey().toString(), e -> e.getValue().predictedChangeCount()));
        return StringTableFormatter.formatData("Predictions", "File", "Predicted change", values, true);
    }
}
