package de.viadee.sonarissuescoring.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.viadee.sonarissuescoring.service.PredictionParams;
import de.viadee.sonarissuescoring.service.misc.ParallelismManager;
import de.viadee.sonarissuescoring.service.prediction.PredictionResult;
import de.viadee.sonarissuescoring.service.prediction.PredictionService;
import de.viadee.sonarissuescoring.service.prediction.train.FilePredictionParams;

@RestController
@RequestMapping("files")
public class FileController {
    private final PredictionService predictionService;
    private final ParallelismManager parallelismManager;

    public FileController(PredictionService predictionService, ParallelismManager parallelismManager) {
        this.predictionService = predictionService;
        this.parallelismManager = parallelismManager;
    }

    /**
     * Calculates the a prediction of how much each java file in the given git-repository is going to change over the next N commits (prediction horizon)
     * <p>
     * The predicted value is the number of commits touching the file, scaled from 0 to 1 via its <a href="https://en.wikipedia.org/wiki/Percentile_rank">percentile rank</a>.
     *
     * @param params preferences, like where to fetch data from
     * @return the predicted values and some associated metrics
     */
    @PostMapping(path = "predict")
    public ResponseEntity<PredictionResult> changePrediction(@RequestBody FilePredictionParams params) {
        return parallelismManager.runIfNotAlreadyWaitingAsHttp(params.gitServer().url(),
                () -> predictionService.predict(PredictionParams.of(params.gitServer(), params.predictionHorizon()), params.h2oUrl()));
    }
}
