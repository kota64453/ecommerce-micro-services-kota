FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/notification-service.jar notification-service.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
