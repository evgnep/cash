FROM gradle:6.7.1-jdk11-openj9 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle server:bootJar --no-daemon

FROM openjdk:11

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", \
    "-Dspring.datasource.url=${DB_URL}", \
    "-Dspring.datasource.username=${DB_USERNAME}", \
    "-Dspring.datasource.password=${DB_PASSWORD}", \
    "-jar", "/app/spring-boot-application.jar"]


