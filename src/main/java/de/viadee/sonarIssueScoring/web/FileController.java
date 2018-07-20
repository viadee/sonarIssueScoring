package de.viadee.sonarIssueScoring.web;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.misc.ParallelismManager;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.PredictionService;
import de.viadee.sonarIssueScoring.service.prediction.train.FilePredictionParams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("files")
public class FileController {
    private final PredictionService predictionService;
    private final ParallelismManager parallelismManager;

    public FileController(PredictionService predictionService, ParallelismManager parallelismManager) {
        this.predictionService = predictionService;
        this.parallelismManager = parallelismManager;
    }

    @RequestMapping(path = "predict", method = RequestMethod.POST) public ResponseEntity<PredictionResult> desirability(@RequestBody FilePredictionParams params) {
        return parallelismManager.runIfNotAlreadyWaitingAsHttp(params.gitServer().url(),
                () -> predictionService.predict(PredictionParams.of(params.gitServer(), params.predictionHorizon()), params.h2oUrl()));
    }
}
