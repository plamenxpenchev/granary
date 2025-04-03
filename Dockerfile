FROM maven:3.9.9-eclipse-temurin-21 AS build-image
ENV GRANARY=/usr/src/granary
WORKDIR $GRANARY
COPY src/ $GRANARY/src/
COPY pom.xml $GRANARY/
SHELL ["/bin/bash", "-c"]
RUN ["mvn", "clean", "install", "-DskipTests"]

FROM eclipse-temurin:21-jre
ENV GRANARY=/usr/src/granary
WORKDIR $GRANARY
COPY --from=build-image $GRANARY .
SHELL ["/bin/bash", "-c"]
ENTRYPOINT ["java", "-jar", "/usr/src/granary/target/granary-0.0.1-SNAPSHOT.jar"]
