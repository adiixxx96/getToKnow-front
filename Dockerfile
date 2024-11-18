FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN echo "echo '<settings><servers><server><id>github</id><username>${GITHUB_USERNAME}</username><password>${GITHUB_TOKEN}</password></server></servers></settings>' > /root/.m2/settings.xml" > /tmp/create-settings.sh \
    && chmod +x /tmp/create-settings.sh \
    && /tmp/create-settings.sh
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gtk-front-1.0.1-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]