# Stage 1: Build the JAR
FROM maven:3.9.0-sapmachine-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim-buster
COPY --from=build app/target/coredatareportingservice-si-1.0.1-SNAPSHOT.jar app/
WORKDIR /app
ENTRYPOINT java -jar coredatareportingservice-si-1.0.1-SNAPSHOT.jar



