package de.viadee.sonarIssueScoring.service.desirability;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

/**
 * This is essentially a Map<GitPath,T> but searches by suffixes.
 * <p>
 * This is required to associate sonar components with filenames, because sonar components might refer to subfolders of a repo.
 * The file for "com.viadee:sonarQuest:src/main/java/com/viadee/sonarQuest/entities/AvatarRace.java" is
 * sonarQuest-backend/src/main/java/com/viadee/sonarQuest/entities/AvatarRace.java
 */
class PathSuffixLookup<T> {
    private static final Logger log = LoggerFactory.getLogger(PathSuffixLookup.class);
    private final SortedMap<String, T> reversedPathMap;

    PathSuffixLookup(Map<GitPath, T> real) {
        this.reversedPathMap = real.entrySet().stream().collect(ImmutableSortedMap.toImmutableSortedMap(Ordering.natural(), e -> reverse(e.getKey()), Entry::getValue));
    }

    private static String reverse(GitPath p) {
        return new StringBuilder(p.toString()).reverse().toString();
    }

    T getOrDefault(GitPath path, T orElse) {
        String search = reverse(path);
        SortedMap<String, T> result = reversedPathMap.subMap(search, search + Character.MAX_VALUE);
        if (result.size() > 1)
            log.warn("Multiple results for {}, using first: {}", path, result);
        return Iterables.getFirst(result.values(), orElse);
    }
}
