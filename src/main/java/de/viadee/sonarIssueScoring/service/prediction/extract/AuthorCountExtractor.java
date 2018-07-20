package de.viadee.sonarIssueScoring.service.prediction.extract;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthorCountExtractor implements FeatureExtractor {
    @Override public void extractFeatures(Repo repo, Map<Path, Builder> output) {
        Map<Path, Multiset<String>> fileAuthors = new HashMap<>(); //All authors of a file, with the respective edit count
        Map<Path, String> lastAuthorPerFile = new HashMap<>();
        Multiset<String> totalAuthorCounts = HashMultiset.create(); //How many times each author changed any file. This is not the number of commits.

        repo.commits().forEach(commit -> {
            totalAuthorCounts.add(commit.authorEmail(), commit.diffs().size());

            commit.diffs().forEach((path, type) -> {
                fileAuthors.computeIfAbsent(path, ignore -> HashMultiset.create()).add(commit.authorEmail());
                lastAuthorPerFile.put(path, commit.authorEmail());
            });
        });

        output.forEach((path, out) -> {
            String lastAuthor = lastAuthorPerFile.get(path);
            out.lastAuthor(lastAuthor);
            out.previousAuthorTotalEdits(totalAuthorCounts.count(lastAuthor));

            Multiset<String> thisFileAuthors = fileAuthors.get(path);
            out.totalEditCount(thisFileAuthors.size());
            out.previousAuthorThisFileEdits(thisFileAuthors.count(lastAuthor));
            out.totalEditCount(thisFileAuthors.size());
            out.totalAuthorCount(thisFileAuthors.elementSet().size());
        });
    }
}
