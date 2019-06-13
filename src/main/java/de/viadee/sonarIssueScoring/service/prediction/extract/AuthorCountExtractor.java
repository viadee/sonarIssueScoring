package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;

@Component
public class AuthorCountExtractor implements FeatureExtractor {
    @Override public void extractFeatures(List<Commit> commits, Output out) {
        Map<Path, Multiset<String>> fileAuthors = new HashMap<>(); //All authors of a file, with the respective edit count
        Map<Path, String> lastAuthors = new HashMap<>();
        Multiset<String> totalAuthorCounts = HashMultiset.create(); //How many times each author changed any file. This is not the number of commits.

        commits.forEach(commit -> {
            totalAuthorCounts.add(commit.authorEmail(), commit.diffs().size());

            commit.diffs().forEach((path, type) -> {
                fileAuthors.computeIfAbsent(path, ignore -> HashMultiset.create()).add(commit.authorEmail());
                lastAuthors.put(path, commit.authorEmail());
            });


            commit.content().forEach((path, content) -> {
                String lastAuthor = lastAuthors.get(path);
                Multiset<String> thisFileAuthors = fileAuthors.get(path);

                out.add(commit, path, "previousAuthor", lastAuthor);
                out.add(commit, path, "previousAuthorTotalEdits", totalAuthorCounts.count(lastAuthor));
                out.add(commit, path, "totalEditCount", thisFileAuthors.size());
                out.add(commit, path, "previousAuthorThisFileEdits", thisFileAuthors.count(lastAuthor));
                out.add(commit, path, "totalAuthorCount", thisFileAuthors.elementSet().size());
            });
        });
    }
}
