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
| `app/src/main/java/com/example/myapplication/ui/state/ChouTuanUiState.kt` | **状态层 (UDF)** | 单向数据流不可变状态 `ChouTuanUiState` 与用户意图 `ChouTuanIntent` 契约 |
| `app/src/main/java/com/example/myapplication/domain/model/ChouTuanDomain.kt` | **领域层 (Domain)** | 纯 Kotlin 业务策略 `CommissionStrategy` 与 `TransparentPricingEngine` 零抽成透明计价引擎 |
| `app/src/main/java/com/example/myapplication/data/repository/ChouTuanRepository.kt` | **仓库层 (Repository)** | 遵循依赖倒置原则的离线优先数据仓库，统一协调本地 Room 缓存与远程网关并支持 15 分钟超时关单记录 |
| `app/src/main/java/com/example/myapplication/data/local/ChouTuanDatabase.kt` | **本地持久化层 (Room)** | Room 实体表 (`TenantRoomEntity`, `OrderRoomEntity`) 与 `ChouTuanRoomDao` 数据库契约 |
| `app/src/main/java/com/example/myapplication/data/remote/ChouTuanRetrofitNetwork.kt` | **网络网关层 (Retrofit)** | 对接后端 `gateway-service` / `product-service` / `order-service` 的多租户请求头与接口契约 |
| `app/src/main/java/com/example/myapplication/sdk/LocationAndPaymentSdk.kt` | **第三方 SDK 适配层** | 高德地图球面距离测算 `AMapSphericalUtils` 与温和防催命骑手调度 ETA 算法 |
| `app/src/main/res/values/strings.xml` | **资源配置** | 应用名称配置（`丑团`） |
| `app/build.gradle.kts` | **构建配置** | 应用包名 (`com.choutuan.app`)、SDK 版本与 Compose 依赖 |

## 运行方式
1. 使用 Android Studio（支持 AGP 9.4.1 / Kotlin 2.2+）打开本项目或执行 `git pull`。
2. 直接点击 Run 运行至真机或模拟器，通过顶部角色栏即可在 **用户端 / 商家端(多租户) / 骑手端** 之间实时联动测试。
