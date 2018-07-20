package de.viadee.sonarIssueScoring.service.prediction.extract;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommitTimeExtractor implements FeatureExtractor {
    @Override public void extractFeatures(Repo repo, Map<Path, Builder> output) {
        Map<Path, Double> lastCommitTime = new HashMap<>();
        Map<Path, DayOfWeek> lastCommitDay = new HashMap<>();

        repo.commits().forEach(commit -> commit.diffs().forEach((path, action) -> lastCommitTime.putIfAbsent(path, commit.authorTime())));
        repo.commits().forEach(commit -> commit.diffs().forEach((path, action) -> lastCommitDay.putIfAbsent(path, commit.authorDay())));

        output.forEach((path, out) -> {
            out.previousAuthorCommitTimeOfDay(lastCommitTime.get(path));
            out.previousAuthorCommitWeekday(lastCommitDay.get(path));
        });
    }
}
