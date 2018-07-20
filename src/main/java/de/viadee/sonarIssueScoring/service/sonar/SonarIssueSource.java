package de.viadee.sonarIssueScoring.service.sonar;

import com.google.common.collect.ImmutableList;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.issues.IssuesService;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SonarIssueSource {

    /** Lists all open issues in an open SonarQube server, for a given project */
    public List<Issue> findAll(ServerInfo sonarServer, String projectId) {
        List<Issue> result = new ArrayList<>();

        //Not closeable
        IssuesService issuesService = new IssuesService(
                HttpConnector.newBuilder().url(sonarServer.url()).credentials(sonarServer.user(), sonarServer.password()).build());

        for (int page = 1; ; page++) {
            SearchWsResponse response = issuesService.search(new SearchRequest().
                    setComponentKeys(ImmutableList.of(projectId)).
                    //Warning: if this is change to fetch all issues, the issue count for the desirability score has to be adapted to only count open issues
                            setStatuses(ImmutableList.of("OPEN", "CONFIRMED", "REOPENED", "RESOLVED")).//
                    setPs("500"). //Documentation states less than 500, but 500 is accepted too
                    setP("" + page));
            result.addAll(response.getIssuesList());

            //pages are indexed 1-based
            if (response.getPaging().getPageIndex() * response.getPaging().getPageSize() >= response.getPaging().getTotal())
                break;
        }
        return ImmutableList.copyOf(result);
    }
}
