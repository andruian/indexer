# Andruian Indexer

[![Build Status](https://travis-ci.org/andruian/indexer.svg?branch=master)](https://travis-ci.org/andruian/indexer)

## Installation
- The easiest way to set up the index server is using Docker. Have a look at the provided 
[docker-compose file](docker/docker-compose.yml) which will get you up and running after just a few adjustments.
- The release JARs are published as Releases in this GitHub repository.
- You will want to configure data definition URLs, admin password and Solr/MongoDB by supplying a 
`application.properties` file to the server. An example can be found [here](src/main/resources/application.properties).
The server will automatically use a property file when located in the working directory. Example:
```
/some/path$ ls
application.properties indexer-1.0.0.jar

/some/path$ java -jar indexer-1.0.0.jar 
```

## Publishing a new version
- Edit the version in [build.gradle](build.gradle)
- Build the JAR
- Tag the commit with the version, e.g. `1.2.3`
- Create a GitHub Release, upload the JAR there.


