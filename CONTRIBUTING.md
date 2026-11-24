# Contributing to ChouTuan

感谢为 ChouTuan 贡献代码。在提交 Pull Request 或直接向 `main` 分支合入变更前，请遵循以下架构约束、业务红线与协作规范。

## 1. SOLID 架构分层约束

- **单一职责（SRP）与分层解耦**：
  - **UI / Compose 层**：仅负责状态渲染与用户交互分发，禁止直接持有网络客户端或数据库实例。
  - **ViewModel 层**：负责状态编排与 UI 事件处理，通过 `Repository` 获取数据，不直接依赖具体网络/存储实现细节。
  - **Repository 层**（如 `ChouTuanRepository`）：统一协调本地数据源（`ChouTuanRoomDao`）与远端数据源（`ChouTuanApiService`），并通过注入 `NetworkRetryExecutor` 等组件封装弱网重试与错误日志。
  - **Backend 服务与契约层**：`backend-contract` 与 `backend-service` 保持接口契约与微服务实现分离，禁止跨层绕过契约直接耦合。
- **开闭与依赖倒置（OCP / DIP）**：新增扩展能力优先通过可选构造参数注入默认实现（如 `retryExecutor: NetworkRetryExecutor = NetworkRetryExecutor()`），确保现有调用方与单元测试 100% 向后兼容。

## 2. 业务红线：0% 抽成与门店同价

- **0% 平台抽成**：任何订单计价、分账、结算及展示逻辑中，严禁引入平台抽佣比例或隐性技术服务费。
- **门店同价**：线上商品价格必须与线下门店保持同价，禁止在客户端或服务端计价链路中添加暗箱加价、差异化溢价逻辑。
- **变更审查**：涉及价格计算、优惠券核销或结算金额的改动，必须同步补充双端单元测试以验证 0% 抽成与门店同价约束。

## 3. 提交信息规范（Commit Message）

采用 Conventional Commits 规范：

```text
<type>(<scope>): <简明描述>
```

- 常用 `type`：`feat`、`fix`、`refactor`、`docs`、`test`、`ci`、`build`
- 常用 `scope`：`android`、`backend`、`api`、`k8s`、`makefile`、`readme`
- 示例：`refactor(android): 在ChouTuanRepository集成NetworkRetryExecutor弱网重试能力`

## 4. 本地双端验证步骤

在推送代码或发起合并前，请务必在仓库根目录执行双端自动化测试：

1. **Android 端单元测试**：
   ```bash
   make android-test
   ```
2. **Backend 服务端测试**：
   ```bash
   make backend-test
   ```

确保上述两项命令均以退出码 `0` 通过后，再提交变更。
