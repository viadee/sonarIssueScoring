package de.viadee.sonarIssueScoring.service.prediction.train;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Column;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

import de.viadee.sonarIssueScoring.service.prediction.ModelMetrics;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import water.bindings.H2oApi;
import water.bindings.pojos.ApiParseTypeValuesProvider;
import water.bindings.pojos.FrameKeyV3;
import water.bindings.pojos.LeaderboardV99;
import water.bindings.pojos.ModelKeyV3;
import water.bindings.pojos.ModelMetricsListSchemaV3;
import water.bindings.pojos.ParseV3;

/**
 * Wraps the api to H2O, extending it with unsupported features around AutoML
 */
// Api: http://docs.h2o.ai/h2o/latest-stable/h2o-docs/rest-api-reference.html
class ExtendedH2oApi {
    private static final Logger log = LoggerFactory.getLogger(ExtendedH2oApi.class);
    private final String h2oUrl;
    private final H2oApi h2oApi;
    private final CsvConverter csvConverter;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client = new OkHttpClient();

    public ExtendedH2oApi(CsvConverter csvConverter, ObjectMapper objectMapper, String h2oUrl) {
        this.h2oUrl = h2oUrl;
        checkArgument(!h2oUrl.endsWith("/"), "The url to h2o cannot end with a /");
        this.csvConverter = csvConverter;
        this.objectMapper = objectMapper;
        this.h2oApi = new H2oApi(this.h2oUrl);
    }

    public FrameKeyV3 uploadAndParse(List<Instance> instances) throws IOException {
        FrameKeyV3 raw = postFile(csvConverter.asCSV(instances));

        CsvSchema schema = csvConverter.schema();

        ParseV3 parseParms = new ParseV3();
        parseParms.sourceFrames = new FrameKeyV3[]{raw};
        parseParms.parseType = ApiParseTypeValuesProvider.CSV;
        parseParms.separator = (byte) schema.getColumnSeparator();
        parseParms.checkHeader = 1;
        parseParms.deleteOnDone = true;
        parseParms.numberColumns = schema.size();
        parseParms.columnNames = StreamSupport.stream(schema.spliterator(), false).map(Column::getName).toArray(String[]::new);
        parseParms.columnTypes = StreamSupport.stream(schema.spliterator(), false).map(col -> col.getType() == ColumnType.NUMBER ? "Numeric" : "Enum").toArray(
                String[]::new);

        parseParms.destinationFrame = H2oApi.stringToFrameKey(UUID.randomUUID() + ".hex");
        parseParms.blocking = true;

        checkNotNull(h2oApi.parse(parseParms), "Parsing failed, reason will be in the h2oLogs, search for %s", raw.name);


        return parseParms.destinationFrame;
    }

    public LeaderboardV99 train(FrameKeyV3 train, FrameKeyV3 test, FrameKeyV3 leaderBoardFrame, int runtime, List<String> excluded) throws IOException {
        String projectName = UUID.randomUUID().toString();

        ObjectNode spec = objectMapper.createObjectNode();


        spec.putObject("input_spec").
                put("response_column", "targetEditCountPercentile").
                put("training_frame", train.name).
                put("validation_frame", test.name).
                put("leaderboard_frame", leaderBoardFrame.name).
                put("fold_column", "fold").
                putArray("ignored_columns").add("path").addAll(excluded.stream().map(TextNode::new).collect(Collectors.toList()));

        spec.putObject("build_control").
                put("keep_cross_validation_predictions", false).
                put("keep_cross_validation_models", false).
                put("project_name", projectName).

                putObject("stopping_criteria").
                put("max_runtime_secs", runtime).
                put("stopping_rounds", 3);

        //StackedEnsemble is deactivated because it fails half the time and provides no real benefit
        spec.putObject("build_models").putArray("exclude_algos").add("DRF").add("GLM").add("DeepLearning").add("StackedEnsemble");

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(spec));
        JsonNode res = execute(new Request.Builder().url(h2oUrl + "/99/AutoMLBuilder").post(body).build());
        log.info("AutoMLBuilder result: {}", res);

        waitForCompletion(res.get("job").get("key").get("name").asText());

        return h2oApi.leaderboard(projectName);
    }

    public List<Double> predict(ModelKeyV3 model, FrameKeyV3 data) throws IOException {
        ModelMetricsListSchemaV3 predict = new ModelMetricsListSchemaV3();
        predict.frame = data;
        predict.model = model;
        predict.deviances = false;

        predict = h2oApi.predict(predict);

        FrameKeyV3 predictions = predict.predictionsFrame;
        List<Double> values = downloadPredictions(predictions.name);

        h2oApi.deleteFrame(predictions);
        return values;
    }

    private List<Double> downloadPredictions(String frame) throws IOException {
        String csv = client.newCall(new Request.Builder().url(h2oUrl + "/3/DownloadDataset?frame_id=" + frame).get().build()).execute().body().string();

        List<String> lines = Lists.newArrayList(Splitter.onPattern("\r?\n").split(csv));

        checkState(lines.get(0).equals("\"predict\""), "Unexpected header: %s", lines.get(0));
        lines.remove(0); //Remove header

        if (lines.get(lines.size() - 1).isEmpty()) //Possible trailing newline
            lines.remove(lines.size() - 1);

        return lines.stream().map(Double::valueOf).collect(ImmutableList.toImmutableList());
    }

    public ModelMetrics metrics(ModelKeyV3 model, FrameKeyV3 frame) throws IOException {
        //There is a method to get model metrics, but it only includes mse & rmse
        JsonNode metrics = execute(new Request.Builder().url(h2oUrl + "/3/ModelMetrics/models/" + model.name + "/frames/" + frame.name).get().build()).get(
                "model_metrics").get(0);

        JsonNode importances = execute(new Request.Builder().url(h2oUrl + "/3/Models/" + model.name).get().build()).get("models").get(0).get("output").get(
                "variable_importances").get("data");
        //importances[0] = col names, importances[3] = importance percentage
        ImmutableMap<String, Double> realImportanceMap = IntStream.range(0, importances.get(0).size()).boxed().collect(
                ImmutableMap.toImmutableMap(i -> importances.get(0).get(i).asText(), i -> nanToZero(importances.get(3).get(i).asDouble())));

        //Contains data for all features
        ImmutableMap<String, Double> importanceMapAll = StreamSupport.stream(csvConverter.schema().spliterator(), false).map(Column::getName).filter(
                col -> !col.startsWith("target") && !col.equals("fold") && !col.equals("path")).
                collect(ImmutableMap.toImmutableMap(k -> k, k -> realImportanceMap.getOrDefault(k, 0.0)));

        return ModelMetrics.of(metrics.get("RMSE").asDouble(), metrics.get("r2").asDouble(), metrics.path("mean_residual_deviance").asDouble(), importanceMapAll);
    }

    private static double nanToZero(double in) {return Double.isNaN(in) ? 0 : in;}

    private void waitForCompletion(String jobId) throws IOException {
        log.info("Awaiting completion of: {}", jobId);

        JsonNode response;
        do {
            Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);

            response = execute(new Request.Builder().url(h2oUrl + "/3/Jobs/" + jobId).get().build());
        } while (response.get("jobs").get(0).get("status").asText().equals("RUNNING"));
    }

    private FrameKeyV3 postFile(byte[] data) throws IOException {
        // RestTemplate has some kind of problem here: some trailing multipart boundary is included in the file
        RequestBody content = RequestBody.create(MediaType.parse("text/csv"), data);
        RequestBody requestBody = new Builder().setType(MultipartBody.FORM).addFormDataPart("file", "upload.csv", content).build();
        JsonNode response = execute(new Request.Builder().url(h2oUrl + "/3/PostFile").post(requestBody).build());

        checkState(response.get("total_bytes").asInt() == data.length, "Unequal data length (expected: %s) in response %s", data.length, response);
        return H2oApi.stringToFrameKey(response.get("destination_frame").asText());
    }

    private JsonNode execute(Request request) throws IOException {
        JsonNode res = objectMapper.readValue(client.newCall(request).execute().body().bytes(), JsonNode.class);
        log.trace("Result for {}: {}", request, res);
        return res;
    }

    public void delete(ModelKeyV3... models) throws IOException {
        for (ModelKeyV3 model : models)
            h2oApi.deleteModel(model);
    }

    public void delete(FrameKeyV3... frames) throws IOException {
        for (FrameKeyV3 frame : frames)
            h2oApi.deleteFrame(frame);
    }
}
