package de.viadee.sonarIssueScoring.service.desirability;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 This is essentially a Map<Path,T> but searches by suffixes.
 *
 * This is required to associate sonar components with filenames, because sonar components might refer to subfolders of a repo.
 * The file for "com.viadee:sonarQuest:src/main/java/com/viadee/sonarQuest/entities/AvatarRace.java" is
 *                  sonarQuest-backend/src/main/java/com/viadee/sonarQuest/entities/AvatarRace.java
 */
public class PathSuffixLookup<T> {
    private static final Logger log = LoggerFactory.getLogger(PathSuffixLookup.class);
    private final SortedMap<String, T> reversedPathMap;

    public PathSuffixLookup(Map<Path, T> real) {
        this.reversedPathMap = real.entrySet().stream().collect(
                ImmutableSortedMap.toImmutableSortedMap(Ordering.natural(), e -> convertToUnixAndReverse(e.getKey()), Entry::getValue));
    }

    private static String convertToUnixAndReverse(Path p) {
        StringBuilder reversed = new StringBuilder(p.toString()).reverse();
        return reversed.toString().replace('\\', '/');
    }

    public T getOrDefault(Path path, T orElse) {
        String search = convertToUnixAndReverse(path);
        SortedMap<String, T> result = reversedPathMap.subMap(search, search + Character.MAX_VALUE);
        if (result.size() > 1)
            log.warn("Multiple results for {}, using first: {}", path, result);
        return Iterables.getFirst(result.values(), orElse);
    }
}
