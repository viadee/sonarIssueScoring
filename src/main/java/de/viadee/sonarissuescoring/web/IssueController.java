package de.viadee.sonarissuescoring.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.viadee.sonarissuescoring.service.desirability.DesirabilityResult;
import de.viadee.sonarissuescoring.service.desirability.DesirabilitySource;
import de.viadee.sonarissuescoring.service.desirability.UserPreferences;
import de.viadee.sonarissuescoring.service.misc.ParallelismManager;

@RestController
@RequestMapping("issues")
public class IssueController {
    private final DesirabilitySource desirabilitySource;
    private final ParallelismManager parallelismManager;

    public IssueController(DesirabilitySource desirabilitySource, ParallelismManager parallelismManager) {
        this.desirabilitySource = desirabilitySource;
        this.parallelismManager = parallelismManager;
    }

    /**
     * Calculates and returns the Desirability-Score for each open issue in the given Repository.
     * <p>
     * If there is already a running DS-Calculation for the given sonar project-id, the request is rejected with an error 429.
     *
     * @param preferences settings, like where to get the data
     * @return A map of Sonar-Issue-Key to the issues Desirability-Score and its individual components / ratings
     */
    @PostMapping(path = "desirability")
    public ResponseEntity<DesirabilityResult> desirability(@RequestBody UserPreferences preferences) {
        return parallelismManager.runIfNotAlreadyWaitingAsHttp(preferences.sonarProjectId(), () -> desirabilitySource.calculateIssueDesirability(preferences));
    }
}
