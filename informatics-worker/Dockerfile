FROM amazoncorretto:21-alpine
WORKDIR /opt/app

# Copy the entire target directory to include dependencies
COPY target/ target/

EXPOSE 8989
# Use the JAR file with dependencies
ENTRYPOINT ["java", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8989", "target/informatics-worker-1.0-SNAPSHOT.jar"]