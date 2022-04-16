FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY order-service-1.0.jar order-server.jar
ENTRYPOINT ["java", "-jar", "order-server.jar"]