FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/target/classes/application.yml application.yml

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"] 