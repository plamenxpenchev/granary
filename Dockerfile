FROM maven:3.9.9-eclipse-temurin-21 AS build-image
ENV GRANARY=/usr/src/granary-build
WORKDIR $GRANARY
COPY annotations/ $GRANARY/annotations/
COPY granary/ $GRANARY/granary/
COPY persistence/ $GRANARY/persistence/
COPY persistence-test/ $GRANARY/persistence-test/
COPY properties/ $GRANARY/properties/
COPY server/ $GRANARY/server/
COPY pom.xml $GRANARY/
SHELL ["/bin/bash", "-c"]
RUN ["mvn", "clean", "package", "-DskipTests"]

FROM eclipse-temurin:21-jre
ENV GRANARY=/usr/src/granary-build
WORKDIR $GRANARY
COPY --from=build-image $GRANARY .
SHELL ["/bin/bash", "-c"]
ENTRYPOINT ["java", "-jar", "/usr/src/granary-build/granary/target/granary-0.0.1-SNAPSHOT.jar"]
