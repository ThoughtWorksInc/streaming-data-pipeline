FROM openjdk:8-jdk-alpine
ADD app.jar /app.jar
ENTRYPOINT java  -jar /app.jar --spring.profiles.active=station-status --spring.profiles.active=docker