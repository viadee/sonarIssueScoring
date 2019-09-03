package de.viadee.sonarissuescoring.web;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import de.viadee.sonarissuescoring.service.prediction.FileInformation;
import de.viadee.sonarissuescoring.service.prediction.PredictionResult;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;

@Component
public class PredictionResultMessageConverterPlaintext extends ToStringMessageConverter<PredictionResult> {

    public PredictionResultMessageConverterPlaintext() {
        super(PredictionResult.class);
    }

    @Override protected String write(PredictionResult res) {
        return StringTableFormatter.formatModelMetrics("Validation", res.validationMetrics()) + formatResults(res.results());
    }

    private static String formatResults(Map<GitPath, FileInformation> data) {
        ImmutableMap<String, Double> values = data.entrySet().stream().collect(
                ImmutableMap.toImmutableMap(e -> e.getKey().toString(), e -> e.getValue().predictedChangeCount()));
        return StringTableFormatter.formatData("Predictions", "File", "Predicted change", values, true);
    }
}
