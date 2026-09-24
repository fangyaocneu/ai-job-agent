FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/ai-job-agent.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]