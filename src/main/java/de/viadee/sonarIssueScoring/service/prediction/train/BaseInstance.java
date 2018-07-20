package de.viadee.sonarIssueScoring.service.prediction.train;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Map;

/**
 * A specific instance of data the ML-Algorithm is trained on
 */
@Immutable
@ImmutableStyle
@JsonSerialize
@JsonDeserialize
public abstract class BaseInstance {
    /** target variables */
    public abstract double targetEditCountPercentile();

    /** No actual argument, required to later bring predictions and paths back together */
    public abstract Path path();

    /** Fold for Cross-Validation for this instance. This is assigned statically via the hash value */
    public abstract int fold();

    public abstract String lastAuthor();

    public abstract String prefix();

    public abstract String suffix();

    //Not named package because that's a keyword
    @Default public String packageDef() {return "";}

    @Default public double random() {return Math.random();}

    public abstract double totalAuthorCount();

    public abstract double totalEditCount();

    public abstract double previousAuthorTotalEdits();

    public abstract double previousAuthorThisFileEdits();

    public abstract double previousAuthorCommitTimeOfDay();

    public abstract DayOfWeek previousAuthorCommitWeekday();

    @Default public int cyclomaticComplexity() {return 0;}

    @Default public int numberOfMethods() {return 0;}

    @Default public int numberOfComments() {return 0;}

    @Default public int linesOfCode() {return 0;}

    public abstract int dependenciesProject();

    public abstract int dependenciesExternal();

    public abstract int dependants();

    @JsonIgnore public abstract Map<CommitAge, Integer> commitAge();

    @JsonIgnore public abstract Map<CommitAge, Integer> commitAgePackage();

    //This construct of ignoring map fields and adding them back is required due to jackson-csv inability to handle maps
    //See also CsvConverter.java
    @JsonAnyGetter public Map<String, Integer> _flattenedMapsForJackson() {
        Builder<String, Integer> builder = ImmutableMap.builder();
        commitAge().forEach((age, val) -> builder.put("commitAge." + age, val));
        commitAgePackage().forEach((age, val) -> builder.put("commitAgePackage." + age, val));
        return builder.build();
    }
}
