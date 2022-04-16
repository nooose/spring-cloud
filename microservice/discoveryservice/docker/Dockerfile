FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY discoveryservice-1.0.jar discovery-server.jar
ENTRYPOINT ["java", "-jar", "discovery-server.jar"]