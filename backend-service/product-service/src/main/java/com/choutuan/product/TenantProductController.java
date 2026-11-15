package com.choutuan.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product-service/api/v1")
public class TenantProductController {

    private static final double EARTH_RADIUS_METERS = 6371000.0;
    private static final BigDecimal COMPETITOR_COMMISSION_RATE = new BigDecimal("0.23");
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final Map<String, TenantStore> tenantStoreMap = new ConcurrentHashMap<>();
    private final Map<String, ProductItem> productIndexMap = new ConcurrentHashMap<>();

    public TenantProductController() {
        initSeedData();
    }

    private void initSeedData() {
        TenantStore t1 = new TenantStore("T1001", "川味堂食小馆(南山店)", "FOOD", 22.5405, 113.9345);
        TenantStore t2 = new TenantStore("T1002", "鲜果茶饮工坊(科技园店)", "DRINK", 22.5432, 113.9410);
        TenantStore t3 = new TenantStore("T1003", "邻里生鲜优选(高新南店)", "SUPERMARKET", 22.5368, 113.9502);

        addProductInternal(t1, new ProductItem("P1001", "T1001", "招牌麻婆豆腐套餐", "FOOD",
                new BigDecimal("28.00"), new BigDecimal("28.00"), true, 120));
        addProductInternal(t1, new ProductItem("P1002", "T1001", "水煮牛肉(堂食同价)", "FOOD",
                new BigDecimal("48.00"), new BigDecimal("48.00"), true, 65));
        addProductInternal(t2, new ProductItem("P2001", "T1002", "手打柠檬鸭屎香茶", "DRINK",
                new BigDecimal("16.00"), new BigDecimal("16.00"), true, 200));
        addProductInternal(t3, new ProductItem("P3001", "T1003", "精选冰鲜鸡胸肉500g", "SUPERMARKET",
                new BigDecimal("19.90"), new BigDecimal("19.90"), true, 80));

        tenantStoreMap.put(t1.tenantId, t1);
        tenantStoreMap.put(t2.tenantId, t2);
        tenantStoreMap.put(t3.tenantId, t3);
    }

    private void addProductInternal(TenantStore store, ProductItem item) {
        recalculateBreakEven(item);
        store.products.add(item);
        productIndexMap.put(item.productId, item);
    }

    /**
     * 与 Android 客户端 ChouTuanApiService.listTenants 完全对齐：
     * 按频道过滤、LBS 经纬度球面距离排序返回附近 0% 抽成租户门店及堂食同价商品
     */
    @GetMapping("/tenants")
    public ResponseEntity<Map<String, Object>> listTenants(
            @RequestParam(required = false, defaultValue = "ALL") String channel,
            @RequestParam(required = false, defaultValue = "22.5400") Double lat,
            @RequestParam(required = false, defaultValue = "113.9350") Double lng,
            @RequestParam(required = false) String keyword) {

        List<Map<String, Object>> result = tenantStoreMap.values().stream()
                .filter(store -> "ALL".equalsIgnoreCase(channel) || store.channel.equalsIgnoreCase(channel))
                .filter(store -> keyword == null || keyword.isBlank()
                        || store.tenantName.contains(keyword)
                        || store.products.stream().anyMatch(p -> p.productName.contains(keyword)))
                .map(store -> {
                    double distanceMeters = calculateSphericalDistanceMeters(lat, lng, store.lat, store.lng);
                    Map<String, Object> dto = new LinkedHashMap<>();
                    dto.put("tenantId", store.tenantId);
                    dto.put("tenantName", store.tenantName);
                    dto.put("channel", store.channel);
                    dto.put("commissionRate", "0.00");
                    dto.put("commissionLabel", "0%抽成·堂食同价");
                    dto.put("lat", store.lat);
                    dto.put("lng", store.lng);
                    dto.put("distanceMeters", Math.round(distanceMeters));
                    dto.put("products", store.products.stream()
                            .filter(p -> p.available)
                            .map(ProductItem::toMap)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .sorted(Comparator.comparingLong(m -> (Long) m.get("distanceMeters")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(apiSuccess("查询附近0%抽成租户门店成功", result));
    }

    /**
     * 商家端：新增/更新堂食同价商品，并自动测算对比某团 23% 抽成的保本溢价
     */
    @PostMapping("/products/upsert")
    public ResponseEntity<Map<String, Object>> upsertProduct(@RequestBody UpsertProductRequest req) {
        String tenantId = (req.tenantId == null || req.tenantId.isBlank()) ? "T1001" : req.tenantId;
        TenantStore store = tenantStoreMap.computeIfAbsent(tenantId,
                id -> new TenantStore(id, "新增合作门店(" + id + ")",
                        req.channel != null ? req.channel : "FOOD", 22.5400, 113.9350));

        String productId = (req.productId == null || req.productId.isBlank())
                ? "P" + System.currentTimeMillis()
                : req.productId;

        BigDecimal dineInPrice = req.dineInPrice != null ? req.dineInPrice : BigDecimal.ZERO;
        BigDecimal takeoutPrice = req.takeoutPrice != null ? req.takeoutPrice : dineInPrice;
        boolean available = req.available == null || req.available;
        int stock = req.stock != null ? req.stock : 100;

        ProductItem item = productIndexMap.get(productId);
        if (item == null) {
            item = new ProductItem(productId, tenantId, req.productName,
                    req.channel != null ? req.channel : store.channel,
                    dineInPrice, takeoutPrice, available, stock);
            addProductInternal(store, item);
        } else {
            item.productName = req.productName != null ? req.productName : item.productName;
            item.channel = req.channel != null ? req.channel : item.channel;
            item.dineInPrice = dineInPrice;
            item.takeoutPrice = takeoutPrice;
            item.available = available;
            item.stock = stock;
            recalculateBreakEven(item);
        }

        return ResponseEntity.ok(apiSuccess("堂食同价商品保存成功，已完成某团23%抽成保本溢价测算", item.toMap()));
    }

    /**
     * 商家端：一键估清 / 上架接口
     */
    @RequestMapping(value = "/products/{productId}/availability", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> updateAvailability(
            @PathVariable String productId,
            @RequestBody AvailabilityRequest req) {

        ProductItem item = productIndexMap.get(productId);
        if (item == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("code", 404);
            err.put("message", "商品不存在: " + productId);
            err.put("data", null);
            err.put("timestamp", Instant.now().toEpochMilli());
            return ResponseEntity.status(404).body(err);
        }

        boolean targetAvailable = req.available != null ? req.available : !item.available;
        item.available = targetAvailable;
        if (!targetAvailable) {
            item.stock = 0;
        } else if (item.stock <= 0) {
            item.stock = 100;
        }

        String msg = targetAvailable ? "商品已一键上架" : "商品已一键估清";
        return ResponseEntity.ok(apiSuccess(msg, item.toMap()));
    }

    private void recalculateBreakEven(ProductItem item) {
        BigDecimal divisor = ONE.subtract(COMPETITOR_COMMISSION_RATE); // 0.77
        BigDecimal breakEvenPrice = item.dineInPrice.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal premiumAmount = breakEvenPrice.subtract(item.dineInPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal premiumPercent = item.dineInPrice.compareTo(BigDecimal.ZERO) > 0
                ? premiumAmount.multiply(new BigDecimal("100"))
                        .divide(item.dineInPrice, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        item.competitorBreakEvenPrice = breakEvenPrice;
        item.competitorBreakEvenPremiumAmount = premiumAmount;
        item.competitorBreakEvenPremiumPercent = premiumPercent;
    }

    private double calculateSphericalDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private Map<String, Object> apiSuccess(String message, Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 200);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", Instant.now().toEpochMilli());
        return body;
    }

    public static class UpsertProductRequest {
        public String tenantId;
        public String productId;
        public String productName;
        public String channel;
        public BigDecimal dineInPrice;
        public BigDecimal takeoutPrice;
        public Boolean available;
        public Integer stock;
    }

    public static class AvailabilityRequest {
        public Boolean available;
    }

    private static class TenantStore {
        final String tenantId;
        final String tenantName;
        final String channel;
        final double lat;
        final double lng;
        final List<ProductItem> products = new CopyOnWriteArrayList<>();

        TenantStore(String tenantId, String tenantName, String channel, double lat, double lng) {
            this.tenantId = tenantId;
            this.tenantName = tenantName;
            this.channel = channel;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static class ProductItem {
        final String productId;
        final String tenantId;
        String productName;
        String channel;
        BigDecimal dineInPrice;
        BigDecimal takeoutPrice;
        boolean available;
        int stock;
        BigDecimal competitorBreakEvenPrice;
        BigDecimal competitorBreakEvenPremiumAmount;
        BigDecimal competitorBreakEvenPremiumPercent;

        ProductItem(String productId, String tenantId, String productName, String channel,
                    BigDecimal dineInPrice, BigDecimal takeoutPrice, boolean available, int stock) {
            this.productId = productId;
            this.tenantId = tenantId;
            this.productName = productName;
            this.channel = channel;
            this.dineInPrice = dineInPrice;
            this.takeoutPrice = takeoutPrice;
            this.available = available;
            this.stock = stock;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productId", productId);
            m.put("tenantId", tenantId);
            m.put("productName", productName);
            m.put("channel", channel);
            m.put("dineInPrice", dineInPrice);
            m.put("takeoutPrice", takeoutPrice);
            m.put("samePriceAsDineIn", dineInPrice.compareTo(takeoutPrice) == 0);
            m.put("available", available);
            m.put("stock", stock);
            m.put("competitorCommissionRate", "23%");
            m.put("competitorBreakEvenPrice", competitorBreakEvenPrice);
            m.put("competitorBreakEvenPremiumAmount", competitorBreakEvenPremiumAmount);
            m.put("competitorBreakEvenPremiumPercent", competitorBreakEvenPremiumPercent + "%");
            return m;
        }
    }
}
