FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gtk-front-1.0.1-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]