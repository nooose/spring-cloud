# spring-cloud

## Spring Boot 2.6.4 & Java 11
### Service discovery (discoveryservice)
* Eureka 
* Port: 8761

### User service (user-service)
* Port: 9001 (default)
* Port: 9002 ~ 9004

#### 여러 개의 Instance 기동 방법들
##### 포트를 직접 등록
```bash
# 1. 실행
./gradlew bootRun --args='--server.port={PORT_NUMBER}'
 
# 2. 빌드 후 실행
./gradlew build
java -jar -Dserver.port={PORT_NUMBER} build/libs/user-service-0.0.1-SNAPSHOT.jar
```
##### Random 포트 사용
```bash
# application.yaml
server.port: 0
```
