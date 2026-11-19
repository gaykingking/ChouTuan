package com.choutuan.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 丑团多租户商家 0% 抽成自助入驻与商家/骑手 0 手续费秒级提现 Controller
 * 对应客户端 OnboardingAndWithdrawScreen 页面接口契约
 */
@RestController
@RequestMapping("/order-service/api/v1")
public class MerchantOnboardingAndWithdrawController {

    public record TenantOnboardRequest(
            String brandCode,
            String shopName,
            String category,
            String contactPhone,
            String creditCode,
            boolean signedSamePricePact
    ) {}

    public record WithdrawApplyRequest(
            String accountRole,     // MERCHANT 或 RIDER
            String targetChannel,   // WECHAT_WALLET / ALIPAY / BANK_CARD
            BigDecimal amountYuan
    ) {}

    /**
     * 1. 多租户商家 0% 佣金抽成自助入驻接口
     * 强制校验《100%堂食同价不涨价公约》电子签署状态
     */
    @PostMapping("/tenants/onboard")
    public ResponseEntity<Map<String, Object>> onboardTenant(
            @RequestBody TenantOnboardRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (!request.signedSamePricePact()) {
            body.put("code", 400);
            body.put("message", "入驻失败：必须签署《100%堂食同价不涨价公约》");
            return ResponseEntity.badRequest().body(body);
        }

        String assignedTenantId = "tenant_" + UUID.randomUUID().toString().substring(0, 8);
        body.put("code", 200);
        body.put("message", "0%抽成租户自助入驻审核通过");
        body.put("tenantId", assignedTenantId);
        body.put("shopName", request.shopName());
        body.put("category", request.category());
        body.put("commissionRate", BigDecimal.ZERO);
        body.put("fixedTechFee", new BigDecimal("0.20"));
        body.put("approvedAt", LocalDateTime.now().toString());
        return ResponseEntity.ok(body);
    }

    /**
     * 2. 商家租户 / 配送骑手 0 手续费秒级提现接口
     */
    @PostMapping("/withdraw/apply")
    public ResponseEntity<Map<String, Object>> applyZeroFeeWithdraw(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "tenant_food_1") String tenantId,
            @RequestBody WithdrawApplyRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (request.amountYuan() == null || request.amountYuan().compareTo(BigDecimal.ZERO) <= 0) {
            body.put("code", 400);
            body.put("message", "提现金额必须大于 0");
            return ResponseEntity.badRequest().body(body);
        }

        String withdrawOrderId = "WD" + System.currentTimeMillis();
        body.put("code", 200);
        body.put("message", "0手续费秒级提现已到账");
        body.put("withdrawOrderId", withdrawOrderId);
        body.put("tenantId", tenantId);
        body.put("accountRole", request.accountRole());
        body.put("targetChannel", request.targetChannel());
        body.put("applyAmount", request.amountYuan());
        body.put("withdrawFee", BigDecimal.ZERO);
        body.put("netArrivedAmount", request.amountYuan());
        body.put("arrivedAt", LocalDateTime.now().toString());
        return ResponseEntity.ok(body);
    }
}
