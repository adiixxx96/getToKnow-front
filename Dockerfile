FROM maven:3.8.4-openjdk-17-slim
WORKDIR /app
COPY pom.xml .
COPY src/ ./src
COPY .m2 ./.m2
RUN mvn -s .m2/settings.xml clean install -DskipTests
COPY target/gtk-front-1.0.1-exec.jar /app/gtk-front-1.0.1-exec.jar
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "gtk-front-1.0.1-exec.jar"]