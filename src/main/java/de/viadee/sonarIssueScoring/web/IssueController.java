package de.viadee.sonarIssueScoring.web;

import de.viadee.sonarIssueScoring.service.desirability.DesirabilitySource;
import de.viadee.sonarIssueScoring.service.desirability.IssueDesirability;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.misc.ParallelismManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("issues")
public class IssueController {
    private final DesirabilitySource desirabilitySource;
    private final ParallelismManager parallelismManager;

    public IssueController(DesirabilitySource desirabilitySource, ParallelismManager parallelismManager) {
        this.desirabilitySource = desirabilitySource;
        this.parallelismManager = parallelismManager;
    }

    //Maps issue key to desirability
    @RequestMapping(path = "desirability", method = RequestMethod.POST) public ResponseEntity<Map<String, IssueDesirability>> desirability(
            @RequestBody UserPreferences preferences) {

        return parallelismManager.runIfNotAlreadyWaitingAsHttp(preferences.sonarProjectId(), () -> desirabilitySource.calculateIssueDesirability(preferences));
    }
}
