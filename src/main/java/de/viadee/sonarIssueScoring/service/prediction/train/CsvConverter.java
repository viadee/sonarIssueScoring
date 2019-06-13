package de.viadee.sonarIssueScoring.service.prediction.train;

import static com.google.common.base.Preconditions.*;

import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;

/**
 * Takes a list of instances, outputs them as csv suitable for h2o
 */
@Service
public class CsvConverter {

    private enum ColType {
        Numeric,
        Enum
    }

    public ImmutableSet<String> colNames(Iterable<Instance> instances) {
        return createSchema(instances).keySet();
    }

    /**
     * @return Map for all columns to their respective type
     */
    private static ImmutableMap<String, ColType> createSchema(Iterable<Instance> instances) {
        return Streams.stream(instances).flatMap(i -> i.data().entrySet().stream()).
                collect(ImmutableMap.toImmutableMap(Entry::getKey, CsvConverter::classify, (a, b) -> {
                    checkState(a == b, "column switched type");
                    return a;
                }));
    }

    private static ColType classify(Entry<?, ?> e) {
        if (e.getValue() instanceof Number)
            return ColType.Numeric;
        if (e.getValue() instanceof String || e.getValue() instanceof Enum<?> || e.getValue() instanceof Boolean)
            return ColType.Enum;
        throw new RuntimeException("Unexpected value: " + e.getValue() + " of type " + e.getValue().getClass() + " for key " + e.getKey());
    }

    public CSVResult toCSV(Iterable<Instance> instances) {
        ImmutableMap<String, ColType> schema = createSchema(instances);

        String header = String.join(",", schema.keySet()) + "\n";
        String content = Streams.stream(instances).map(i -> schema.entrySet().stream().map(
                e -> e.getValue() == ColType.Numeric ? i.data().get(e.getKey()).toString() : quote(i.data().get(e.getKey()).toString())).
                collect(Collectors.joining(","))).collect(Collectors.joining("\n"));

        return new CSVResult() {
            @Override public String[] colNames() {
                return schema.keySet().toArray(new String[0]);
            }

            @Override public String[] colTypes() {
                return schema.values().stream().map(Enum::name).toArray(String[]::new);
            }

            @Override public byte[] data() {
                return (header + content).getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    interface CSVResult {
        String[] colNames();

        String[] colTypes();

        byte[] data();
    }

    private String quote(String in) {
        String QUOTE = "\"";
        return QUOTE + in.replace(QUOTE, QUOTE + QUOTE) + QUOTE;
    }
}
