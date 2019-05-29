package de.viadee.sonarIssueScoring.web;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import de.viadee.sonarIssueScoring.service.desirability.IssueDesirability;

@Component
public class DesirabilityMapMessageConverterPlaintext extends ToStringMessageConverter<Map<String, IssueDesirability>> {

    public DesirabilityMapMessageConverterPlaintext() {
        super(Map.class);
    }

    @Override protected String write(Map<String, IssueDesirability> res) {
        Map<String, Double> scoreMap = Maps.transformValues(res, IssueDesirability::desirabilityScore);

        return StringTableFormatter.formatData("Issue Desirability", "Issue", "Desirability Score", scoreMap, true);
    }
}
