package de.viadee.sonarissuescoring.service.desirability;

import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import org.springframework.data.annotation.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
public class DesirabilityResult {
    private final ImmutableMap<String, IssueDesirability> desirabilities;

    public DesirabilityResult(Map<String, IssueDesirability> desirabilities) {
        this.desirabilities = ImmutableMap.copyOf(desirabilities);
    }

    @JsonProperty public ImmutableMap<String, IssueDesirability> desirabilities() {
        return desirabilities;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DesirabilityResult that = (DesirabilityResult) o;
        return desirabilities.equals(that.desirabilities);
    }

    @Override public int hashCode() {
        return Objects.hash(desirabilities);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("desirabilities", desirabilities).toString();
    }
}
