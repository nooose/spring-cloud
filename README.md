# spring-cloud

## Spring Boot 2.6.4 & Java 11
### Service discovery (discoveryservice)
* Eureka 
* Port: 8761


### API Gateway
- JWT 인증 필터
#### 로드밸런싱 목록
* user-serivce
* catalog-service
* order-service

### User service (user-service)
* Port: 9001 (default)
* Port: 9002 ~ 9004
* User 생성, 조회 기능
* order-service 요청 (feign)
* 로그인 기능 POST 요청 (Spring security) 
    - JWT 발급

### Catalog Service
* 상품 등록, 조회 기능
### Order Service
* Catalog Service에 있는 상품을 User Service에 등록된 user로 주문이 가능
* Order 생성, 조회 기능

### Config-service
* config 디렉터리에 config 파일 저장



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

---
# Kafka 
## 서버 기동
```bash
# Zookeeper
$KAFKA_HOME/bin/zookeeper-server-start.sh  $KAFKA_HOME/config/zookeeper.properties

# Kafka
$KAFKA_HOME/bin/kafka-server-start.sh  $KAFKA_HOME/config/server.properties
```
## Kafka 사용
```bash
# Create topic
$KAFKA_HOME/bin/kafka-topics.sh --create --topic $TOPIC_NAME --bootstrap-server localhost:9092 --partitions 1

# Get topic list
$KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# Describe topic
$KAFKA_HOME/bin/kafka-topics.sh --describe --topic $TOPIC_NAME --bootstrap-server localhost:9092

# Prdoucer
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic $TOPIC_NAME

# Consumer
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic $TOPIC_NAME --from-beginning
```
## Kafka connector
* DB 동기화를 위해 사용

1. 실행
    ```bash
    $KAFKA_CONNECTOR_HOME/bin/connect-distributed.sh $KAFKA_CONNECTOR_HOME/etc/kafka/connect-distributed.properties
    ```
1. plugin 추가
    .\etc\kafka\connect-distributed.properties 파일 마지막에 아래 plugin 정보 추가

    `plugin.path=~/confluentinc-kafka-connect-jdbc-10.4.0/lib`

1. MariaDB 드라이버 복사
    * JdbcSourceConnector에서 MariaDB를 사용하기 위해 MariaDB 드라이버 복사
    * **Gradle 외부 라이브러리 위치**
     
        `~/.gradle/caches/modules-2/files-2.1/org.mariadb.jdbc/mariadb-java-client/2.7.2/{tmp_name}/mariadb-java-client-2.7.2.jar`

### MariaDB
* Kafka Source connector, Sink Connector에서 사용할 DB
```bash
# 시작
mysql.server start
# 중지
mysql.server stop
# 상태 출력
mysql.server status

# 접속
mysql –uroot
```
> Access denided 해결
```bash
sudo mysql –u root
```
```SQL
use mysql;
select user, host, plugin FROM mysql.user;
set password for 'root'@'localhost'=password('test1357’);
flush privileges;

/* mydb에 생성 */
create table users(
    id int auto_increment primary key,
    user_id varchar(20),
    password varchar(20),
    name varchar(20),
    created_at datetime default NOW()
);

create table orders (
    id int auto_increment primary key,
    product_id varchar(20) not null,
    qty int default 0,
    unit_price int default 0,
    total_price int default 0,
    user_id varchar(50) not null,
    order_id varchar(50) not null,
    created_at datetime default NOW()
);
```
### Source Connect
* Connector 등록
    ```bash
    echo '
    {
        "name" : "my-source-connect",

        "config" : {
            "connector.class" : "io.confluent.connect.jdbc.JdbcSourceConnector",
            "connection.url":"jdbc:mysql://localhost:3306/mydb",
            "connection.user":"root",
            "connection.password":"test1357",
            "mode": "incrementing",
            "incrementing.column.name" : "id",
            "table.whitelist":"users",
            "topic.prefix" : "my_topic_",
            "tasks.max" : "1"
        }
    }' | curl -X POST -d @- http://localhost:8083/connectors --header "content-Type:application/json"
    ```

### Sink Connect
* Connector 등록
    ```bash
    echo '
    {
        "name":"my-sink-connect",
        "config":{
            "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
            "connection.url":"jdbc:mysql://localhost:3306/mydb",
            "connection.user":"root",
            "connection.password":"test1357",
            "auto.create":"true",
            "auto.evolve":"true",
            "delete.enabled":"false",
            "tasks.max":"1",
            "topics":"my_topic_users"
        }
    }'| curl -X POST -d @- http://localhost:8083/connectors --header "content-Type:application/json"
    ```

# 컨테이너 배포

## 도커 네트워크
```bash
# 가상 네트워크 생성
docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 $NETWORK_NAME

# 네트워크 리스트
docker network ls

# 특정 네트워크 확인
docker network inspect $NETWORK_NAME
```


## 컨테이너 실행
```bash
# config-service
docker run -d -p 8888:8888 --network $NETWORK_NAME \
    --name config-service config-service:1.0

# discovery-service
docker run -d -p 8761:8761 --network $NETWORK_NAME \
    -e "spring.cloud.config.uri=http://config-service:8888" \
    --name discovery-service discovery-service:1.0

# apigateway-service
docker run -d -p 8000:8000 --network $NETWORK_NAME \
    -e "spring.cloud.config.uri=http://config-service:8888" \
    -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
    --name apigateway-service apigateway-service:1.0


```
> MariaDB Dockerfile
```Docker
FROM mariadb
ENV MYSQL_ROOT_PASSWORD test1357
ENV MYSQL_DATABASE mydb
# COPY ./mysql_data/mysql /var/lib/mysql
EXPOSE 3306
ENTRYPOINT ["mysqld", "--user=root"]
```
```bash
# MariaDB
docker run -d -p 3306:3306  --network $NETWORK_NAME \
    --name mariadb mariadb:1.0
```

```bash
# zookeepr & kafka
# kafka 디렉터리에 위치
docker-compose up -d

# zipkin
docker run -d -p 9411:9411 \
    --network $NETWORK_NAME \
    --name zipkin \
    openzipkin/zipkin


# Prometheus
docker run -d -p 9090:9090 \
    --network $NETWORK_NAME \
    --name prometheus \
    -v /path/to/prometheus.yml:/etc/prometheus/prometheus.yml \
    prom/prometheus 

# Grafana
docker run -d -p 3000:3000 \
    --network $NETWORK_NAME \
    --name grafana \
    grafana/grafana     

# user-service
docker run -d --network $NETWORK_NAME \
    --name user-service \
    -e "spring.cloud.config.uri=http://config-service:8888" \
    -e "spring.zipkin.base-url=http://zipkin:9411" \
    -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
    -e "logging.file=/api-logs/users-ws.log" \
    user-service:1.0

# order-service
docker run -d --network $NETWORK_NAME \
    --name order-service \
    -e "spring.zipkin.base-url=http://zipkin:9411" \
    -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
    -e "spring.datasource.url=jdbc:mariadb://mariadb:3306/mydb" \
    -e "logging.file=/api-logs/orders-ws.log" \
    order-service:1.0

# catalog-service
docker run -d --network $NETWORK_NAME \
    --name catalog-service \
    -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
    -e "logging.file=/api-logs/catalogs-ws.log" \
    catalog-service:1.0
```