# spring-cloud

## Spring Boot 2.6.4 & Java 11
### Service discovery (discoveryservice)
* Eureka 
* Port: 8761


### API Gateway
#### 로드밸런싱 목록
* user-serivce
* catalog-service
* order-service

### User service (user-service)
* Port: 9001 (default)
* Port: 9002 ~ 9004
* User 생성, 조회 기능

### Catalog Service
* 상품 등록, 조회 기능
### Order Service
* Catalog Service에 있는 상품을 User Service에 등록된 user로 주문이 가능
* Order 생성, 조회 기능



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
