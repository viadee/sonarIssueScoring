package de.viadee.sonarIssueScoring.service.prediction.train;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator.Feature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Takes a list of instances, outputs them as csv suitable for h2o
 */
@Service
public class CsvConverter {
    private final CsvMapper csvMapper;
    private final CsvSchema schema;

    public CsvConverter() {
        //This is no bean, because it's also an ObjectMapper, which clashes with the default ObjectMapper config
        this.csvMapper = new CsvMapper().configure(Feature.ALWAYS_QUOTE_STRINGS, true);

        Builder builder = CsvSchema.builder().setUseHeader(true);

        //Auto schema detection is a bit wonky, detecting numbers as strings.
        for (Method method : BaseInstance.class.getDeclaredMethods()) {
            Class<?> type = method.getReturnType();
            if (type != void.class && type != Map.class)
                builder.addColumn(method.getName(), type.isPrimitive() && type != boolean.class ? ColumnType.NUMBER : ColumnType.STRING);
        }

        //Keep in sync with BaseInstance.java
        for (CommitAge commitAge : CommitAge.values()) {
            builder.addColumn("commitAge." + commitAge, ColumnType.NUMBER);
            builder.addColumn("commitAgePackage." + commitAge, ColumnType.NUMBER);
        }

        this.schema = builder.build();
    }

    public byte[] asCSV(List<Instance> instances) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SequenceWriter writer = csvMapper.writerFor(Instance.class).with(schema).writeValues(baos)) {
            writer.writeAll(instances);
        }
        return baos.toByteArray();
    }

    public CsvSchema schema() {return schema;}
}
