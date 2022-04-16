FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY user-service-1.0.jar user-server.jar
ENTRYPOINT ["java", "-jar", "user-server.jar"]