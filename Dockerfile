FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
COPY settings.xml /root/.m2/settings.xml
RUN ls -l /root/.m2/ && cat /root/.m2/settings.xml
RUN mvn -s /root/.m2/settings.xml \
    -Dgithub.token=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 \
    clean install -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gtk-front-1.0.1-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]