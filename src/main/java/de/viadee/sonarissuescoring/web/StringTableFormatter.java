package de.viadee.sonarissuescoring.web;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import de.viadee.sonarissuescoring.service.prediction.ModelMetrics;

public class StringTableFormatter {
    public static String formatData(String title, String keyHeader, String valueHeader, Map<String, Double> data, boolean orderDesc) {
        int maxKeyLength = data.keySet().stream().mapToInt(String::length).max().orElse(0);
        int keyColLength = Math.max(maxKeyLength, keyHeader.length());

        String out = title + ":\n";
        out += String.format("%" + keyColLength + "s | %s\n", keyHeader, valueHeader);
        out += Strings.repeat("-", keyColLength + 1) + "+" + Strings.repeat("-", 1 + valueHeader.length()) + "\n";

        Ordering<String> order = orderDesc ? Ordering.from(Comparator.comparing(data::get)).reverse() : Ordering.natural();
        List<String> keys = order.sortedCopy(data.keySet());

        for (String key : keys)
            out += String.format(Locale.US, "%" + keyColLength + "s | %.3f\n", key, data.get(key));

        return out + "\n";
    }

    public static String formatModelMetrics(String type, ModelMetrics in) {
        Map<String, Double> metrics = ImmutableMap.of("RMSE", in.rmse(), "R2", in.r2(), "MeanResidualDeviance", in.meanResidualDeviance());
        return formatData(type + " Metrics", "Metric", "Value", metrics, false) +//
                formatData(type + "variable importances", "Variable", "Importance", in.variableImportances(), true);
    }
}
