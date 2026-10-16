
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/ai-job-agent.jar app.jar

# Package Agent Skills into the container
COPY skills/ /app/skills/

ENTRYPOINT ["java", "-jar", "app.jar"]
