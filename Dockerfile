#
# Build stage
#
FROM maven:3.9.5-jdk-21 AS build
COPY . .
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM openjdk:21-jdk-slim
COPY --from=build /target/WeightWise-0.0.1-SNAPSHOT.jar WeightWise.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]