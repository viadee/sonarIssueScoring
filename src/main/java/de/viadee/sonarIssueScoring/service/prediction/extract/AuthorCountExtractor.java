package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;

@Component
public class AuthorCountExtractor implements FeatureExtractor {
    @Override public void extractFeatures(Repo repo, Output out) {
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

        out.add(path -> {
            String lastAuthor = lastAuthorPerFile.get(path);
            Multiset<String> thisFileAuthors = fileAuthors.get(path);
            return ImmutableMap.<String, Object>of(//
                    "lastAuthor", lastAuthor,//
                    "previousAuthorTotalEdits", totalAuthorCounts.count(lastAuthor),//
                    "totalEditCount", thisFileAuthors.size(),//
                    "previousAuthorThisFileEdits", thisFileAuthors.count(lastAuthor),//
                    "totalAuthorCount", thisFileAuthors.elementSet().size());
        });
    }
}
