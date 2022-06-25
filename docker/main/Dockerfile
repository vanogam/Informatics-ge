FROM openjdk:8-jdk-alpine

#ARG JAR_FILE=informatics-system/target/*.jar

WORKDIR /opt/app

COPY docker/run.sh run.sh
RUN ["chmod", "+x", "/opt/app/run.sh"]
ENTRYPOINT ["/opt/app/run.sh"]