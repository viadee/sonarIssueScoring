package de.viadee.sonarissuescoring.service.prediction.extract;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.viadee.sonarissuescoring.service.prediction.load.Commit;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;

@Component
public class CommitTimeExtractor implements FeatureExtractor {
    @Override public void extractFeatures(List<Commit> commits, Output out) {
        Map<GitPath, Double> lastCommitTime = new HashMap<>();
        Map<GitPath, DayOfWeek> lastCommitDay = new HashMap<>();

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
