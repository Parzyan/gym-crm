FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -pl gym-crm -am clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=no-integration

COPY --from=build /app/gym-crm/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
