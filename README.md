# SonarIssueScoring

[![Build Status](https://travis-ci.org/viadee/sonarIssueScoring.svg?branch=master)](https://travis-ci.org/viadee/sonarIssueScoring)

This is a tool for prioritizing [SonarQube](https://www.sonarqube.org/) issues, to fix the most important technical debt first.
 It is mainly intended to be used with [SonarQuest](https://github.com/viadee/sonarQuest), but can be used standalone as well.

This tool incorporates a prediction of which files in a repository will change, this is usable without SonarQuest as well.


## Getting started

Clone the repository, and execute `mvn install exec:java` in a command line. Additionally, a running [H2O-Server](https://h2o.ai) (version [3.20.0.3](http://h2o-release.s3.amazonaws.com/h2o/latest_stable.html) or greater) is required. 

This is all required setup for [SonarQuest](https://github.com/viadee/sonarQuest), alternatively the REST-API can be used directly: 

Ordering issues by their desirability to be solved (entry point: [IssueController](src/main/java/de/viadee/sonarIssueScoring/web/IssueController.java)): 

```bash
curl -d '{"sonarServer":{"url":"https://sonarcloud.io"},"sonarProjectId":"commons-io:commons-io","predictionHorizon": 256,"gitServer": {"url":"https://github.com/apache/commons-io"},"h2oUrl":"http://localhost:54321"}' -H "Content-Type: application/json" -X POST http://localhost:5432/issues/desirability
```

Alternatively, only the change count can be predicted (entry point: [FileController](src/main/java/de/viadee/sonarIssueScoring/web/FileController.java)):

```bash
curl -d '{"predictionHorizon": 256,"gitServer": {"url":"https://github.com/apache/commons-io"},"h2oUrl":"http://localhost:54321"}' -H "Content-Type: application/json" -X POST http://localhost:5432/files/predict
```

## Functionality

This tool consists of two major parts, the first one being the Desirability-Score for calculating the importance / desire for a fix of a specific issue.
An issue can be made more important by a lot of different criteria, such as a high SonarQube-Severity rating. Additionally, an issue is more important if the containing
file is edited soon - therefore the second large part is the prediction of whether a file changes.
  
### Desirability Score

The Desirability-Score of an issue consists of a number of different Ratings for different aspects. Each rating represents the importance of an issue with this specific 
aspect in contrast to the same issue without this aspect. For instance, an issue regarding security is 1.5 times more important than an issue not regarding security.  

Thus, the final Desirability-Score is the product of all ratings, and expresses the importance of an issue over a hypothetical issue with no interesting properties except its existence.

A number of different Ratings are used: 
* Age: An issue is more important if it is younger, to promote [fixing the leak](https://docs.sonarqube.org/display/SONAR/Fixing+the+Water+Leak). 
  Additionally, younger issues are more likely to be still in the developers head, and therefore faster to fix.
* Effort: This is used to deprioritize issues with a very high required effort for fixing them, as they leave less room for other refactoring
* Severity: Issues with a high severity by SonarQube are naturally more pressing to fix.
* RuleType: Bugs are more important than code smells.
* Tags: Different tags have different assigned ratings - for instance security issues are more pressing to fix.
* Number of issues in a file: A file which has a lot of issues should be refactored soon, as otherwise the problem could worsen, as the class is "bad anyways".
* Centrality: If a file is used by a lot of other files, it should be  fixed sooner, to avoid spreading bad design decisions.
* Directory / Package: Directories can manually be set as more important, to focus on specific parts of the application.
* ChangePrediction: Most issues are way less relevant if the file is not going to be edited again. As this is information from the future, it can only be predicted.

The Desirability-Score is primarily implemented in de.viadee.sonarIssueScoring.service.desirability, the entry point is [DesirabilitySource.java](src/main/java/de/viadee/sonarIssueScoring/service/desirability/DesirabilitySource.java)

### Change Prediction

The Change Prediction uses a number of different features, which are extracted out of the git-history of the predicted project. 
For this, the git history is linearized, only changes to the master branch are used. All changes to any side branch are only considered on merging.

These features are created for different times in the past - all this data is used to train a [H2O](https://h2o.ai) model. 

This model is then used to predict the actual future - the result is fed into the Desirability-Score.

This is primarily implemented in de.viadee.sonarIssueScoring.service.prediction, the entry point is [PredictionService.java](src/main/java/de/viadee/sonarIssueScoring/service/prediction/PredictionService.java).

## Development setup

This project uses [org.immutables](https://immutables.github.io), which is an annotation processor.
It might require [setup](https://immutables.github.io/apt.html) in an IDE.

## License
This project is licensed under the BSD 3-Clause "New" or "Revised" License - see the [LICENSE](LICENSE) file for details.

The licenses of the reused components can be found in the [dependency overview](https://rawgit.com/viadee/sonarIssueScoring/master/docs/MavenSite/dependencies.html).

Additionally, [DependencyVisitor.java](src/main/java/de/viadee/sonarIssueScoring/service/prediction/extract/DependencyVisitor.java) is mostly a copy of 
[CBO.java](https://github.com/mauricioaniche/ck/blob/master/src/main/java/com/github/mauricioaniche/ck/metric/CBO.java) from the Apache2-Licensed [ck](https://github.com/mauricioaniche/ck) library
 

