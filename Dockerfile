FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/ai-job-agent.jar app.jar

ENTRYPOINT ["java", "-Dloader.main=com.fangyao.agent.DailyJobRunner", "-cp", "app.jar", "org.springframework.boot.loader.launch.PropertiesLauncher"]