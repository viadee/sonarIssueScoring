package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;

@Component
public class CommitTimeExtractor implements FeatureExtractor {
    @Override public void extractFeatures(Repo repo, Output out) {
        Map<Path, Double> lastCommitTime = new HashMap<>();
        Map<Path, DayOfWeek> lastCommitDay = new HashMap<>();

        repo.commits().forEach(commit -> commit.diffs().forEach((path, action) -> lastCommitTime.putIfAbsent(path, commit.authorTime())));
        repo.commits().forEach(commit -> commit.diffs().forEach((path, action) -> lastCommitDay.putIfAbsent(path, commit.authorDay())));

        out.add("previousAuthorCommitTimeOfDay", lastCommitTime::get); //
        out.add("previousAuthorCommitWeekday", lastCommitDay::get); //
    }
}
