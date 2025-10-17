FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -pl trainer-workload -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=no-integration

COPY --from=build /app/trainer-workload/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
