package de.viadee.sonarIssueScoring.service.prediction;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.prediction.load.RepositoryLoader;
import de.viadee.sonarIssueScoring.service.prediction.load.SplitRepository;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;
import de.viadee.sonarIssueScoring.service.prediction.train.MLInput;
import de.viadee.sonarIssueScoring.service.prediction.train.MLService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PredictionService {

    private final RepositoryLoader repositoryLoader;
    private final InstanceSource instanceSource;
    private final MLService mlService;

    public PredictionService(RepositoryLoader repositoryLoader, InstanceSource instanceSource, MLService mlService) {
        this.repositoryLoader = repositoryLoader;
        this.instanceSource = instanceSource;
        this.mlService = mlService;
    }

    public PredictionResult predict(PredictionParams params, String h2oServer) {
        SplitRepository data = repositoryLoader.loadSplitRepository(params);

        List<Instance> instances = instanceSource.extractInstances(data.trainingData());
        List<Instance> predictableInstances = instanceSource.extractInstances(data.completePast());

        return mlService.predict(MLInput.of(instances, predictableInstances, h2oServer));
    }
}

