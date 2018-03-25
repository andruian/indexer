FROM openjdk:8-jdk
COPY ./ /indexer-build
WORKDIR /indexer-build
RUN ./gradlew bootJar && rm -rf /root/.gradle && mkdir -p /indexer && mv build/libs/indexer*.jar /indexer/

WORKDIR /indexer
CMD ["/bin/sh", "-c", "java -jar indexer*.jar"]
