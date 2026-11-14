# 丑团 (ChouTuan) Spring Cloud 后端微服务接口与表结构契约文档

本文档与 Android 客户端 `ChouTuanApiService` 保持完全契约对齐，基于 **Spring Boot 3 + Spring Cloud + MySQL 8.0 分表 (`t_order_0` / `t_order_1`) + RocketMQ 5.x** 构建，严格落实平台 **0% 佣金抽成、仅收 ¥0.20 固定支付通道成本、骑手 100% 实收配送费** 的透明分账规则。

---

## 1. 全局请求头与统一响应契约

### 1.1 多租户与端侧公共请求头 (Aligned with `ChouTuanApiService`)
| Header | 必填 | 示例值 | 说明 |
| :--- | :---: | :--- | :--- |
| `Authorization` | 是 | `Bearer <jwt_token>` | OAuth2 / JWT 鉴权令牌 |
| `X-Tenant-Id` | 是 | `tenant_sz_001` | 多租户隔离标识（城市/商圈租户ID） |
| `X-Role-Type` | 是 | `CUSTOMER` / `MERCHANT` / `RIDER` | 调用方端侧角色 |
| `X-Idempotency-Key` | 下单/支付必填 | `uuid-v4` | 幂等防重放键 |

### 1.2 统一响应体 `ApiResponse<T>`
```json
{
  "code": 0,
  "message": "OK",
  "traceId": "ct-20261114-9a8f1c",
  "data": {}
}
```

---

## 2. 核心微服务 RESTful 接口契约 (`ChouTuanApiService` 对齐)

| 模块 | 方法与路径 | 说明 | 核心请求/响应字段 |
| :--- | :--- | :--- | :--- |
| **订单创建** | `POST /api/v1/orders` | 用户提交订单（触发15分钟关单延迟消息） | `merchantId`, `items[]`, `goodsAmountFen`, `packingFeeFen`, `deliveryFeeFen`, `channelFeeFen` (固定 `20`) |
| **订单详情** | `GET /api/v1/orders/{orderId}` | 查询订单状态与透明费用拆分 | `orderStatus`, `payStatus`, `merchantNetFen`, `riderNetFen`, `channelFeeFen` |
| **取消订单** | `POST /api/v1/orders/{orderId}/cancel` | 用户/系统超时关单 | `cancelReason` |
| **商家接单** | `POST /api/v1/merchant/orders/{orderId}/accept` | 商家确认接单并触发“丑团新订单0抽成到账”播报 | `merchantId`, `estimatedPrepMinutes` |
| **商家结算单** | `GET /api/v1/merchant/ledgers` | 查询商家 0 抽成逐笔到账明细 | `page`, `size`, `totalGoodsFen`, `platformCommissionFen` (恒为 `0`) |
| **骑手接单** | `POST /api/v1/rider/tasks/{orderId}/accept` | 骑手抢单并触发“新配送任务全额运费到账”播报 | `riderId`, `deliveryFeeFen` |
| **骑手送达** | `POST /api/v1/rider/tasks/{orderId}/complete` | 确认送达并驱动分账终态解冻 | `riderId`, `deliveredAt` |
| **支付分账** | `POST /api/v1/payments/notify/{channel}` | 微信/支付宝回调（`WECHAT` / `ALIPAY`） | `outTradeNo`, `transactionId`, `totalFen` |

---

## 3. MySQL 分表结构设计 (`t_order_0` / `t_order_1`)

路由策略：按 `order_id % 2`（ShardingSphere-JDBC 水平分片）路由至 `t_order_0` 与 `t_order_1`，所有金额字段统一使用 `BIGINT`（单位：分）避免精度误差。

```sql
-- 订单分表模板：分别创建 t_order_0 与 t_order_1
CREATE TABLE `t_order_0` (
  `order_id` BIGINT NOT NULL COMMENT '雪花算法分布式订单ID（分片键）',
  `tenant_id` VARCHAR(32) NOT NULL COMMENT '多租户ID',
  `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `rider_id` BIGINT DEFAULT NULL COMMENT '骑手ID',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1待接单 2制作中 3配送中 4已完成 5已关闭',
  `goods_amount_fen` BIGINT NOT NULL COMMENT '商品货款（分，100%归商家）',
  `packing_fee_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '打包费（分，100%归商家）',
  `delivery_fee_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '配送费（分，100%归骑手）',
  `channel_fee_fen` BIGINT NOT NULL DEFAULT 20 COMMENT '固定支付通道成本（分，固定20分）',
  `platform_commission_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '平台抽成（恒为0）',
  `total_pay_fen` BIGINT NOT NULL COMMENT '用户实付总额（分）',
  `pay_channel` VARCHAR(16) DEFAULT NULL COMMENT 'WECHAT / ALIPAY',
  `expire_at` DATETIME NOT NULL COMMENT '支付超时时间（创建+15分钟）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `idx_tenant_merchant_status` (`tenant_id`, `merchant_id`, `order_status`),
  KEY `idx_tenant_rider_status` (`tenant_id`, `rider_id`, `order_status`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='丑团订单分表0（t_order_1结构完全一致）';
```

### 3.1 透明分账流水表 (`t_profit_sharing_ledger`)
```sql
CREATE TABLE `t_profit_sharing_ledger` (
  `ledger_id` BIGINT NOT NULL COMMENT '分账流水ID',
  `tenant_id` VARCHAR(32) NOT NULL COMMENT '多租户ID',
  `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
  `pay_channel` VARCHAR(16) NOT NULL COMMENT 'WECHAT / ALIPAY',
  `merchant_net_fen` BIGINT NOT NULL COMMENT '商家实收 = goods_amount_fen + packing_fee_fen',
  `rider_net_fen` BIGINT NOT NULL COMMENT '骑手实收 = delivery_fee_fen',
  `channel_cost_fen` BIGINT NOT NULL DEFAULT 20 COMMENT '通道成本 = 20分',
  `sharing_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待分账 1分账中 2分账成功 3分账失败',
  `channel_tx_no` VARCHAR(64) DEFAULT NULL COMMENT '微信/支付宝分账单号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信/支付宝透明分账事务流水表';
```

---

## 4. RocketMQ 异步消息与事务分账契约

### 4.1 15分钟超时关单延迟消息
- **Topic**: `ct-order-timeout-delay-topic`
- **Producer**: `order-service` 创建订单落库后发送延迟消息（RocketMQ `DelayTimeLevel = 16` 或 5.x 任意秒级定时 `deliveryTimestamp = now + 900_000ms`）。
- **Consumer (`order-timeout-consumer`)**:
  1. 根据 `orderId % 2` 路由查询 `t_order_0` / `t_order_1`。
  2. 幂等校验：仅当 `order_status == 0`（待支付）时执行 `UPDATE ... SET order_status = 5 WHERE order_id = ? AND order_status = 0`。
  3. 释放库存并推送关闭通知；若已支付或已取消则直接 ACK 跳过。

### 4.2 微信/支付宝透明分账事务消息
- **Topic**: `ct-payment-profit-sharing-tx-topic`
- **流程 (Half-Message 两阶段提交)**:
  1. **发送 Half 消息**：`payment-service` 收到微信/支付宝支付成功回调后，发送分账事务半消息（Payload 含 `orderId`, `tenantId`, `payChannel`, `merchantNetFen`, `riderNetFen`, `channelCostFen=20`）。
  2. **执行本地事务**：在同一个本地数据库事务中更新 `t_order_{0|1}.order_status = 1` 并插入 `t_profit_sharing_ledger`（`sharing_status = 0`）。
  3. **提交 / 回查**：本地事务提交成功返回 `COMMIT`，RocketMQ 投递消息至 `settlement-service` 调用微信分账 / 支付宝统一收单分账接口；若发生网络抖动，Broker 回查 `t_profit_sharing_ledger` 存在性决定 `COMMIT` 或 `ROLLBACK`。
  4. **消费幂等**：`settlement-service` 基于 `uk_order_id` 保证分账指令仅执行一次，确保商家 0 抽成与骑手全额运费准确到账。
