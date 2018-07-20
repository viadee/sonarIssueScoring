package de.viadee.sonarIssueScoring.service.desirability;

public enum RatingType {
    Age("Age of the sonar. Younger sonar => higher score"),
    Effort("Effort to fix the sonar. Lower effort => higher score"),
    Severity("SonarQube-Severity of the sonar. Higher severity => higher score"),
    RuleType("Type of the sonar: Code Smell (low score), Vulnerability(medium score), Bug (high socre)"),
    TagsSecurity("Issue has security tags"),
    TagsBug("Issue has tags indicating the presence of bugs"),
    TagsHardToUnderstand("Issue has tags indicating code is hard to understand"),
    TagsPossibleFutureProblems("Issue has tags indicating pitfalls for the future developer"),
    NumberOfIssuesInFile("Total number of issues the file has. More issues => higher score"),
    Centrality("Number of files depending on this file. More dependents => higher score"),
    Directory("Directory of the sonar. Score is set manually"),
    ChangePrediction("Predicted change count of this file. Higher prediction => higher score");

    private final String description;

    RatingType(String description) {
        this.description = description;
    }
}
