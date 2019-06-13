package de.viadee.sonarIssueScoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;
import de.viadee.sonarIssueScoring.service.prediction.EvaluationResult;
import de.viadee.sonarIssueScoring.service.prediction.PredictionService;
import de.viadee.sonarIssueScoring.web.EvaluationResultPrinter;
import de.viadee.sonarIssueScoring.web.PredictionResultMessageConverterPlaintext;

@Component
public class Evaluator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(Evaluator.class);

    private final PredictionService predictionService;
    private final PredictionResultMessageConverterPlaintext prettyPrinter;

    public Evaluator(PredictionService predictionService, PredictionResultMessageConverterPlaintext prettyPrinter) {
        this.predictionService = predictionService;
        this.prettyPrinter = prettyPrinter;
    }

    /**
     * Evaluates the prediction quality versus the actual future on a given sample project.
     */
    @Override public void run(ApplicationArguments args) {
        if (willRunEvaluation(args)) {
            log.info("Starting evaluation. No web server is started."); //Web server is disabled in SonarIssueScoringApplication

            String repo = args.containsOption("repo") ? args.getOptionValues("repo").get(0) : null;
            if (repo == null) {
                log.info("No repository provided, using default");
                repo = "https://github.com/apache/commons-lang";
            }

            String user = args.containsOption("user") ? args.getOptionValues("user").get(0) : null;
            String password = args.containsOption("password") ? args.getOptionValues("password").get(0) : null;

            int horizon = args.containsOption("horizon") ? Integer.parseInt(args.getOptionValues("horizon").get(0)) : 384;

            ServerInfo server = ServerInfo.of(repo, user, password);

            log.info("Running evaluation for {} with horizon", predictionService); //Password is redacted automatically
            EvaluationResult result = predictionService.evaluate(PredictionParams.of(server, horizon), "http://localhost:54321");
            log.info("Evaluation result: \n{}", EvaluationResultPrinter.asString(result));
        }
    }

    /** Static, because the context is not yet setup when this is called. */
    public static boolean willRunEvaluation(ApplicationArguments args) {
        return args.containsOption("evaluate");
    }
}
