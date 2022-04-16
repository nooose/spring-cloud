FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY apigateway-service-1.0.jar apigateway-server.jar
ENTRYPOINT ["java", "-jar", "apigateway-server.jar"]