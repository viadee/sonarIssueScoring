package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;

@Component
public class CommitTimeExtractor implements FeatureExtractor {
    public void extractFeatures(List<Commit> commits, Output out) {
        Map<Path, Double> lastCommitTime = new HashMap<>();
        Map<Path, DayOfWeek> lastCommitDay = new HashMap<>();

        commits.forEach(c -> {
            c.diffs().keySet().forEach(path -> {
                lastCommitDay.put(path, c.authorDay());
                lastCommitTime.put(path, c.authorTime());
            });
            c.content().keySet().forEach(path -> {
                out.add(c, path, "previousAuthorCommitTimeOfDay", lastCommitDay.get(path));
                out.add(c, path, "previousAuthorCommitWeekday", lastCommitTime.get(path));
            });
        });
    }
}
