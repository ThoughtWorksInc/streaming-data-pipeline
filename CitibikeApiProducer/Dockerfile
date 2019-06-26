FROM openjdk:8-jdk-alpine
ADD build/libs/free2wheelers-citibike-apis-producer0.1.0.jar /app.jar

ARG active_station
ENV station=$active_station

ENTRYPOINT java  -jar /app.jar --spring.profiles.active=$station --spring.profiles.active=docker