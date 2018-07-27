package de.viadee.sonarIssueScoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo.Builder;
import de.viadee.sonarIssueScoring.service.prediction.EvaluationResult;
import de.viadee.sonarIssueScoring.service.prediction.PredictionService;

@Component
public class Evaluator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(Evaluator.class);

    private final PredictionService predictionService;

    public Evaluator(PredictionService predictionService) {this.predictionService = predictionService;}

    /**
     * Evaluates the prediction quality versus the actual future on a given sample project.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (willRunEvaluation(args)) {
            log.info("Starting evaluation. No web server is started."); //Web server is disabled in SonarIssueScoringApplication

            Builder builder = ServerInfo.builder();

            if (args.containsOption("repo"))
                builder.url(args.getOptionValues("repo").get(0));
            else {
                log.info("No repository provided, using default");
                builder.url("https://github.com/apache/commons-lang");
            }

            if (args.containsOption("user"))
                builder.user(args.getOptionValues("user").get(0));

            if (args.containsOption("password"))
                builder.password(args.getOptionValues("password").get(0));

            int horizon = 384;
            if (args.containsOption("horizon"))
                horizon = Integer.parseInt(args.getOptionValues("horizon").get(0));

            ServerInfo server = builder.build();

            log.info("Running evaluation for {} with horizon", predictionService); //Password is redacted automatically
            EvaluationResult result = predictionService.evaluate(PredictionParams.of(server, horizon), "http://localhost:54321");
            log.info("Evaluation result {}", result);
        }
    }

    /** Static, because the context is not yet setup when this is called. */
    public static boolean willRunEvaluation(ApplicationArguments args) {
        return args.containsOption("evaluate");
    }
}
