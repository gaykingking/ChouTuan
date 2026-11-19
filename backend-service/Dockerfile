# ============================================================
# Stage 1: Maven Build
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

ARG SERVICE_NAME=order-service
WORKDIR /build

COPY . .
RUN mvn -pl ${SERVICE_NAME} -am clean package -DskipTests

# ============================================================
# Stage 2: Lightweight JRE Runtime (K3s + SkyWalking Ready)
# ============================================================
FROM eclipse-temurin:17-jre-alpine

ARG SERVICE_NAME=order-service
WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS=""

# Reserve mount point for SkyWalking Java Agent in K3s
# Example JAVA_OPTS: -javaagent:/skywalking/agent/skywalking-agent.jar -Dskywalking.agent.service_name=choutuan-order
RUN mkdir -p /skywalking/agent

COPY --from=builder /build/${SERVICE_NAME}/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]
