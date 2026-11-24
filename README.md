# 丑团 (ChouTuan) — 反高抽成、拒绝两头吃的多租户原生安卓平台

基于 **Kotlin + Jetpack Compose + Material 3** 构建，遵循 **SOLID 原则** 与 **UDF 单向数据流** 分层架构。内置 **用户端 / 多租户商家端 / 骑手端** 一键切换闭环，涵盖餐饮外卖、丑团闪购、生鲜超市、到店团购、平价买药全频道。

## 核心设计理念
- **0% 平台佣金抽成**：商家 100% 实收货款与打包费，拒绝逼迫商家涨价凑满减。
- **100% 门店/堂食同价**：商品页直接对比传统高抽成平台溢价，公开透明账单去向（仅收固定 ¥0.20 支付通道成本）。
- **配送费 100% 归骑手**：骑手全额拿运费，采用温和配送 ETA 缓冲算法，拒绝极限压榨。

## 项目目录与模块说明（哪个文件是什么）

| 模块路径 | 层级 | 作用说明 |
| :--- | :--- | :--- |
| `app/src/main/java/com/example/myapplication/MainActivity.kt` | **表现层 (UI)** | 应用主入口与 Jetpack Compose 全量交互界面（全频道首页、多规格弹窗、LBS 轨迹雷达、透明分账弹窗、多租户商家端、骑手大厅、SDK 联调台） |
| `app/src/main/java/com/example/myapplication/ui/screens/UserChannelHomeScreen.kt` | **表现层 (UI Screen)** | 独立用户端全频道首页组件（餐饮外卖、丑团闪购、生鲜超市、平价买药） |
| `app/src/main/java/com/example/myapplication/ui/screens/GroupBuyVoucherScreen.kt` | **表现层 (UI Screen)** | 独立到店团购与8位核销码页面（0抽成真直降、待核销/已核销券列表） |
| `app/src/main/java/com/example/myapplication/ui/screens/OrderRadarTrackScreen.kt` | **表现层 (UI Screen)** | 独立高德 LBS 订单配送轨迹雷达页面 |
| `app/src/main/java/com/example/myapplication/ui/screens/MerchantWorkspaceScreen.kt` | **表现层 (UI Screen)** | 独立多租户商家端管理工作台页面 |
| `app/src/main/java/com/example/myapplication/ui/screens/RiderDeliveryScreen.kt` | **表现层 (UI Screen)** | 独立骑手大厅页面（集成温和 ETA 提示） |
| `app/src/main/java/com/example/myapplication/ui/screens/SdkConsoleScreen.kt` | **表现层 (UI Screen)** | 独立 Room / Retrofit / 高德地图 / 支付 SDK 联调控制台 |
| `app/src/main/java/com/example/myapplication/ui/screens/OnboardingAndWithdrawScreen.kt` | **表现层 (UI Screen)** | 多租户商家 0% 抽成自助入驻表单、签署《100%堂食同价不涨价公约》及商家/骑手 0 手续费秒级提现至微信零钱/支付宝/银行卡账单面板 |
| `app/src/main/java/com/example/myapplication/ui/state/ChouTuanUiState.kt` | **状态层 (UDF)** | 单向数据流不可变状态 `ChouTuanUiState` 与用户意图 `ChouTuanIntent` 契约 |
| `app/src/main/java/com/example/myapplication/domain/model/ChouTuanDomain.kt` | **领域层 (Domain)** | 纯 Kotlin 业务策略 `CommissionStrategy` 与 `TransparentPricingEngine` 零抽成透明计价引擎 |
| `app/src/main/java/com/example/myapplication/data/repository/ChouTuanRepository.kt` | **仓库层 (Repository)** | 遵循依赖倒置原则的离线优先数据仓库，统一协调本地 Room 缓存与远程网关并支持 15 分钟超时关单记录 |
| `app/src/test/java/com/example/myapplication/ChouTuanRepositoryTest.kt` | **单元测试 (Unit Test)** | 基于 JUnit 4 与 `InMemoryChouTuanRoomDao` 验证 `ChouTuanRepository` 离线缓存同步、15 分钟超时关单缓存写入及诊断日志记录 | |
| `app/src/main/java/com/example/myapplication/data/local/ChouTuanDatabase.kt` | **本地持久化层 (Room)** | Room 实体表 (`TenantRoomEntity`, `OrderRoomEntity`) 与 `ChouTuanRoomDao` 数据库契约 |
| `app/src/main/java/com/example/myapplication/data/remote/ChouTuanRetrofitNetwork.kt` | **网络网关层 (Retrofit)** | 对接后端 `gateway-service` / `product-service` / `order-service` 的多租户请求头与接口契约 |
| `app/src/main/java/com/example/myapplication/sdk/LocationAndPaymentSdk.kt` | **第三方 SDK 适配层** | 高德地图球面距离测算 `AMapSphericalUtils` 与温和防催命骑手调度 ETA 算法 |
| `app/src/main/res/values/strings.xml` | **资源配置** | 应用名称配置（`丑团`） |
| `app/build.gradle.kts` | **构建配置** | 应用包名 (`com.choutuan.app`)、SDK 版本与 Compose 依赖 |
| `app/src/main/java/com/example/myapplication/sdk/VoiceBroadcastManager.kt` | **第三方 SDK 适配层** | Android TTS 语音播报管理器，用于商户接单与核销语音提醒 |
| `backend-contract/ChouTuanSpringCloudContract.md` | **后端接口契约** | Spring Cloud 微服务接口契约与多租户透明分账规范文档 |
| `backend-service/gateway-service/src/main/java/com/choutuan/gateway/filter/MultiTenantGatewayFilter.java` | **微服务网关层 (Gateway)** | Spring Cloud Gateway 全局过滤器，校验 `X-App-Id`/`X-Tenant-Id`/`X-Client-Role` 并注入 `X-Trace-Id` 与 `X-Commission-Policy: ZERO_COMMISSION` |
| `backend-service/order-service/src/main/java/com/choutuan/order/TransparentOrderController.java` | **订单微服务控制层** | 透明订单与零抽成结算核心 REST 控制器 |
| `backend-service/order-service/src/main/java/com/choutuan/order/MerchantOnboardingAndWithdrawController.java` | **后端订单微服务 (Spring Boot)** | 多租户商家 0% 抽成自助入驻审核与商家/骑手 0 手续费秒级提现接口 
| `backend-service/order-service/src/main/java/com/choutuan/order/mq/ProfitSharingTransactionListener.java` | **分布式事务层 (RocketMQ)** | RocketMQ 半事务消息监听器，保障透明分账台账最终一致性 |
| `backend-service/order-service/src/main/resources/application-sharding.yml` | **分库分表配置** | ShardingSphere 按 `user_id % 2` 分片路由与多租户数据源配置 |
| `backend-service/order-service/src/main/resources/schema-sharding.sql` | **数据库初始化脚本** | MySQL 多租户商户、订单分片表、透明分账台账分片表及到店团购核销券表 DDL 脚本 |
| `backend-service/product-service/src/main/java/com/choutuan/product/TenantProductController.java` | **商品微服务控制层** | 对齐 `ChouTuanApiService.listTenants` 的多租户门店 LBS 球面距离检索、堂食同价商品管理（含某团 23% 抽成保本溢价测算）与一键估清/上架 REST 控制器 |
| `k8s/choutuan-k3s-deployment.yaml` | **K3s 集群部署清单** | 适配 Debian 11 + K3s 集群的多租户微服务（`gateway-service` / `product-service` / `order-service`）部署清单，内置 Nacos、RocketMQ 与 SkyWalking 链路追踪参数 |
| `.github/workflows/android-ci.yml` | **CI/CD 自动化流水线** | GitHub Actions 自动构建脚本，在 `push`/`pull_request` 到 `main` 时配置 JDK 17、运行 `./gradlew testDebugUnitTest` 与 `./gradlew assembleDebug` 并上传 `choutuan-debug-apk` 产物 |
| `backend-service/notification-service/src/main/java/com/choutuan/notification/RiderMerchantPushConsumer.java` | **实时消息推送服务 (notification-service)** | RocketMQ 消费者与 WebSocket / PushPlus 微信通知分发器，监听订单创建、商家 0 抽成接单、团购核销及骑手全额运费送达事件，向多租户商家端与骑手端推送语音播报指令与微信账单回执  
| `backend-service/pom.xml` | **后端构建层 (Maven)** | Spring Boot 3 + Spring Cloud + Spring Cloud Alibaba（Nacos、RocketMQ、ShardingSphere、MySQL、Spring Web/Gateway）多模块 Maven 父工程配置（包含 `gateway-service`、`product-service`、`order-service`、`notification-service` 四个模块及统一依赖版本管理，JDK 17） |
| `backend-service/Dockerfile` | **容器化构建层 (Docker)** | 支持多微服务复用的多阶段 Docker 构建脚本（`maven:3.9-eclipse-temurin-17` 编译打包 + `eclipse-temurin:17-jre-alpine` 精简运行），通过 `ARG SERVICE_NAME=order-service` 切换服务并预留 `JAVA_OPTS` 适配 K3s 中的 SkyWalking `-javaagent` 挂载参数 |
| `backend-service/docker-compose.yml` | **本地联调编排 (Docker Compose)** | 本地一键启动开发环境编排文件，集成 MySQL 8.0（自动挂载 `schema-sharding.sql` 初始化分表）、Redis 7、Nacos 2.3（`MODE=standalone`）与 RocketMQ NameServer / Broker |
| `backend-service/order-service/pom.xml` | **订单服务构建 (Maven)** | `order-service` 子模块 Maven 依赖配置，继承父工程 `backend-service/pom.xml` 并引入 Spring Boot Web、Nacos Discovery、RocketMQ、ShardingSphere JDBC 与 MySQL 驱动 |
| `backend-service/order-service/src/main/java/com/choutuan/order/OrderServiceApplication.java` | **订单服务入口 (Spring Boot)** | `order-service` 微服务 Spring Boot 启动类，启用 `@SpringBootApplication` 与 `@EnableDiscoveryClient` 注册服务 
| `backend-service/gateway-service/pom.xml` & `GatewayApplication.java` | **网关服务构建与入口 (gateway-service)** | `gateway-service` 子模块 Maven 依赖配置与 Spring Cloud Gateway 微服务启动类（集成 Nacos 服务发现与多租户全局路由过滤） |
| `backend-service/product-service/pom.xml` & `ProductServiceApplication.java` | **商品服务构建与入口 (product-service)** | `product-service` 子模块 Maven 依赖配置与 Spring Boot 微服务启动类（启用 `@SpringBootApplication` 与 `@EnableDiscoveryClient`） |
| `backend-service/notification-service/pom.xml` | **消息推送服务构建 (Maven)** | `notification-service` 子模块 Maven 依赖配置，继承父工程 `backend-service/pom.xml` 并引入 Web、WebSocket、Nacos Discovery 与 RocketMQ 依赖 |
| `backend-service/notification-service/src/main/java/com/choutuan/notification/NotificationServiceApplication.java` | **消息推送服务入口 (Spring Boot)** | `notification-service` 微服务 Spring Boot 启动类，启用 `@SpringBootApplication` 与 `@EnableDiscoveryClient` 注册服务 |
| `backend-service/notification-service/src/main/java/com/choutuan/notification/config/WebSocketBrokerConfig.java` | **STOMP WebSocket 代理配置** | 注册 `/ws/choutuan` STOMP 端点，开启 `/topic/merchant` 与 `/topic/rider` 多租户语音播报订阅前缀 |
| `.github/workflows/backend-ci.yml` | **后端 CI 自动编译流水线** | GitHub Actions 后端自动化构建脚本，在 `backend-service/**` 变更时使用 JDK 17 自动执行 `mvn -B -f backend-service/pom.xml clean package -DskipTests` 验证四大微服务全量编译通过 |
| `app/src/main/java/com/example/myapplication/data/remote/RetrofitClientFactory.kt` | **网络工厂层 (Retrofit)** | 多租户 OkHttp3 拦截器与 Retrofit 客户端工厂，自动注入 `X-App-Id` / `X-Tenant-Id` / `X-Client-Role` 请求头并创建 `ChouTuanApiService` 实例 |
| `backend-service/order-service/src/test/java/com/choutuan/order/MerchantOnboardingAndWithdrawControllerTest.java` | **订单服务单元测试 (JUnit 5)** | `order-service` 商家入驻 0% 抽成审核与秒级提现控制器单元测试 |
| `backend-service/product-service/src/test/java/com/choutuan/product/TenantProductControllerTest.java` | **商品服务单元测试 (JUnit 5)** | `product-service` 多租户门店 LBS 距离检索与堂食同价竞品保本溢价测算单元测试 |
| `app/src/test/java/com/example/myapplication/LocationAndSdkUnitTest.kt` | **Android 端单元测试 (JUnit 4)** | 验证高德球面距离计算（`AMapSphericalUtils`）、多租户请求头拦截器与客户端 SDK 核心逻辑 |
| `backend-service/gateway-service/src/test/java/com/choutuan/gateway/filter/MultiTenantGatewayFilterTest.java` | **网关服务单元测试 (JUnit 5)** | `gateway-service` 多租户全局路由过滤、`X-Tenant-Id` 透传与越权拦截单元测试 |
| `backend-service/notification-service/src/test/java/com/choutuan/notification/RiderMerchantPushConsumerTest.java` | **消息推送服务单元测试 (JUnit 5)** | `notification-service` 验证 `ORDER_CREATED` / `MERCHANT_ACCEPTED_ZERO_COMMISSION` / `RIDER_DELIVERED_FULL_FEE` 多租户语音播报推送 |
| `Makefile` | **全栈一键运维脚本** | 提供 `dev-up` / `dev-down`、`backend-test`、`android-test` / `android-apk`、`docker-build` 与 `k3s-deploy` / `k3s-status` 一键命令 
| `app/src/main/java/com/example/myapplication/ui/screens/RevenueAnalyticsScreen.kt` | **7日收益可视化报表页 (Android)** | 展示商家近7日 0 抽成收益趋势、同行业抽成对比与省佣金额可视化图表 |
| `backend-service/order-service/src/main/java/com/choutuan/order/RevenueAnalyticsController.java` | **7日收益统计控制器 (order-service)** | 提供多租户商家近7日 0 抽成收益与省佣对比统计查询接口 |
| `backend-service/order-service/src/test/java/com/choutuan/order/RevenueAnalyticsControllerTest.java` | **7日收益统计单元测试 (JUnit 5)** | `order-service` 7日收益可视化统计与省佣对比计算控制器单元测试 |
| `backend-service/order-service/src/main/java/com/choutuan/order/exception/GlobalExceptionHandler.java` | **全局异常拦截器（order-service）** | 统一拦截业务校验、参数绑定及系统异常并返回标准化错误响应结构 |
| `backend-service/order-service/src/test/java/com/choutuan/order/exception/GlobalExceptionHandlerTest.java` | **全局异常拦截器单元测试（JUnit 5）** | 验证 `GlobalExceptionHandler` 对各类异常的状态码与错误响应体处理 |
| `backend-service/order-service/src/main/java/com/choutuan/order/health/HealthCheckController.java` | **K3s 健康检查端点（order-service）** | 提供 `GET /order-service/api/v1/health` 健康探针接口，返回服务状态、`ZERO_COMMISSION` 政策与 `0.20` 固定通道费 |
| `backend-service/order-service/src/test/java/com/choutuan/order/health/HealthCheckControllerTest.java` | **健康检查端点单元测试（JUnit 5）** | 使用 `MockMvc` 验证 `GET /order-service/api/v1/health` 返回 HTTP 200 及 `UP`、`ZERO_COMMISSION`、`0.20` 字段 |
| `app/src/test/java/com/example/myapplication/ChouTuanViewModelTest.kt` | **Android ViewModel 单元测试 (JUnit 4)** | 验证 `ChouTuanViewModel` 透明账单试算、0 抽佣策略与状态流更新逻辑 |
| `backend-service/product-service/src/main/java/com/choutuan/product/health/ProductHealthController.java` | **K3s 健康检查端点 (product-service)** | 提供 `GET /product-service/api/v1/health` 健康探针接口，返回服务状态、`ZERO_COMMISSION` 政策与 `0.20` 固定通道费 |
| `backend-service/notification-service/src/main/java/com/choutuan/notification/health/NotificationHealthController.java` | **K3s 健康检查端点 (notification-service)** | 提供 `GET /notification-service/api/v1/health` 健康探针接口，返回服务状态、`ZERO_COMMISSION` 政策与 `0.20` 固定通道费 |
| `backend-service/notification-service/src/test/java/com/choutuan/notification/health/NotificationHealthControllerTest.java` | **健康检查端点单元测试 (JUnit 5)** | 使用 `MockMvc` 验证 `GET /notification-service/api/v1/health` 返回 HTTP 200 及 `UP`、`ZERO_COMMISSION`、`0.20` 字段 |
| `backend-service/product-service/src/test/java/com/choutuan/product/health/ProductHealthControllerTest.java` | **健康检查端点单元测试 (product-service / JUnit 5)** | 使用 `MockMvc` 验证 `GET /product-service/api/v1/health` 返回 HTTP 200 及 `UP`、`ZERO_COMMISSION`、`0.20` 字段 |
| `backend-service/gateway-service/src/main/java/com/choutuan/gateway/health/GatewayHealthController.java` | **K3s 健康检查端点 (gateway-service)** | 提供 WebFlux 响应式 `/health` 健康探针接口，返回 `UP`、`gateway-service`、`ZERO_COMMISSION` 政策与时间戳 |
| `backend-service/gateway-service/src/test/java/com/choutuan/gateway/health/GatewayHealthControllerTest.java` | **健康检查端点单元测试 (gateway-service / JUnit 5)** | 直接调用 `new GatewayHealthController().health().block()` 验证返回 `UP`、`gateway-service`、`ZERO_COMMISSION` 与非空 `timestamp` |
| `k8s/choutuan-k3s-hpa.yaml` | **K3s 高峰期 HPA 自动扩缩容清单** | 基于 `autoscaling/v2` 为 `gateway-service`、`product-service`、`order-service` 配置 CPU 利用率 70% 自动扩缩容（`minReplicas: 1`, `maxReplicas: 4`），适配午/晚高峰外卖流量波峰 | 
| `k8s/choutuan-monitoring-rules.yaml` | **K3s Prometheus 监控告警清单** | 适配 K3s Prometheus + Grafana 监控体系的 `ServiceMonitor` 与 `PrometheusRule` 清单，覆盖 `choutuan-namespace` 四大微服务指标抓取及服务宕机、HTTP 5xx 异常率、午晚高峰 CPU 持续过载与 15 分钟延迟关单积压告警 |
## 运行方式
1. 使用 Android  Studio（支持 AGP 9.4.1 / Kotlin 2.2+）打开本项目或执行 `git pull`。
2. 直接点击 Run 运行至真机或模拟器，通过顶部角色栏即可在 **用户端 / 商家端(多租户) / 骑手端** 之间实时联动测试。
