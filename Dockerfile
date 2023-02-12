FROM adoptopenjdk/openjdk11:alpine-jre

MAINTAINER Durga

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

EXPOSE 80/tcp

WORKDIR /app

ARG JAR_FILE=target/continuum-cms.jar
ARG LIB_DIR=target/libs

COPY ${JAR_FILE} continuum-cms.jar

COPY ${LIB_DIR}/* libs/

ENTRYPOINT ["java", "-jar", "continuum-cms.jar"]

