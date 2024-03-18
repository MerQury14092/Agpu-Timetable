FROM openjdk:17
LABEL authors="merqury"

ENV TIME_ZONE=Europe/Moscow

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

COPY ./target/*.jar .
RUN mv *.jar start.jar

ENTRYPOINT ["java", "-jar", "start.jar"]