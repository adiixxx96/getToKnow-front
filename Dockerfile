FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN echo "<settings>" > /root/.m2/settings.xml && \
    echo "  <servers>" >> /root/.m2/settings.xml && \
    echo "    <server>" >> /root/.m2/settings.xml && \
    echo "      <id>github</id>" >> /root/.m2/settings.xml && \
    echo "      <username>adiixxx96</username>" >> /root/.m2/settings.xml && \
    echo "      <password>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</password>" >> /root/.m2/settings.xml && \
    echo "    </server>" >> /root/.m2/settings.xml && \
    echo "  </servers>" >> /root/.m2/settings.xml && \
    echo "</settings>" >> /root/.m2/settings.xml
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gtk-front-1.0.1-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]