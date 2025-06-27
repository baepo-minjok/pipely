FROM openjdk:17-jdk-slim

WORKDIR /app

COPY ./build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# ENTRYPOINT 수정: JAVA_OPTS 반영
ENTRYPOINT ["java", "-Xms4g", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-XX:InitiatingHeapOccupancyPercent=45", "-jar", "app.jar", "--spring.config.location=/config/application.yml"]