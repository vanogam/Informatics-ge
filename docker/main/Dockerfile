FROM openjdk:21-jdk-slim
WORKDIR /opt/app
ARG VERSION=0.0.1-SNAPSHOT

COPY informatics-system/target/Informatics.jar Informatics.jar
COPY docker/run.sh run.sh
RUN ["chmod", "+x", "/opt/app/run.sh"]
ENTRYPOINT ["/opt/app/run.sh"]