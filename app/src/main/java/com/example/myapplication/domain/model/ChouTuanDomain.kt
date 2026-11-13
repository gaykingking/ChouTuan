package com.example.myapplication.domain.model

import kotlin.math.roundToInt

/**
 * 丑团核心领域层模型与零抽成透明计价引擎（纯 Kotlin，零 Android/UI 耦合）
 */
enum class TransparentCommissionPolicy(val rate: Double, val fixedTechFee: Double) {
    ZERO_COMMISSION(0.0, 0.20)
}

data class TransparentBillBreakdown(
    val goodsTotal: Double,
    val packagingTotal: Double,
    val riderFee: Double,
    val platformTechFee: Double,
    val otherPlatformGoodsTotal: Double,
    val otherCommissionRate: Int
) {
    val userPayTotal: Double
        get() = round2(goodsTotal + packagingTotal + riderFee + platformTechFee)

    val merchantNetIncome: Double
        get() = round2(goodsTotal + packagingTotal)

    val riderNetIncome: Double
        get() = round2(riderFee)

    val userSavedAmount: Double
        get() = round2((otherPlatformGoodsTotal - goodsTotal).coerceAtLeast(0.0))

    val merchantSavedCommission: Double
        get() = round2(goodsTotal * (otherCommissionRate / 100.0))

    private fun round2(v: Double): Double = (v * 100.0).roundToInt() / 100.0
}

object TransparentPricingEngine {
    fun calculateBill(
        storePricesAndCounts: List<Triple<Double, Double, Int>>, // (storePrice, packFee, count)
        otherPlatformPricesAndCounts: List<Pair<Double, Int>>,
        riderFee: Double,
        otherCommissionRate: Int = 23
    ): TransparentBillBreakdown {
        val goods = storePricesAndCounts.sumOf { it.first * it.third }
        val pack = storePricesAndCounts.sumOf { it.second * it.third }
        val otherGoods = otherPlatformPricesAndCounts.sumOf { it.first * it.second }
        return TransparentBillBreakdown(
            goodsTotal = goods,
            packagingTotal = pack,
            riderFee = riderFee,
            platformTechFee = TransparentCommissionPolicy.ZERO_COMMISSION.fixedTechFee,
            otherPlatformGoodsTotal = otherGoods,
            otherCommissionRate = otherCommissionRate
        )
    }
}
