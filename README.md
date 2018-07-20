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

## Development setup

This project uses [org.immutables](https://immutables.github.io), which is an annotation processor.
It might require [setup](https://immutables.github.io/apt.html) in an IDE.

## License
This project is licensed under the BSD 3-Clause "New" or "Revised" License - see the [LICENSE](LICENSE) file for details.

The licenses of the reused components can be found in the [dependency overview](https://rawgit.com/viadee/sonarIssueScoring/master/docs/MavenSite/dependencies.html).

Additionally, [DependencyVisitor.java](src/main/java/de/viadee/sonarIssueScoring/service/prediction/extract/DependencyVisitor.java) is mostly a copy of 
[CBO.java](https://github.com/mauricioaniche/ck/blob/master/src/main/java/com/github/mauricioaniche/ck/metric/CBO.java) from the Apache2-Licensed [ck](https://github.com/mauricioaniche/ck) library
 

