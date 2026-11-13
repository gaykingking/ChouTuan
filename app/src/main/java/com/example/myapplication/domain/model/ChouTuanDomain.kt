package com.example.myapplication.domain.model

import kotlin.math.roundToInt

interface CommissionStrategy {
    val policyName: String
    fun calculatePlatformFee(goodsTotal: Double): Double
    fun calculateTechFee(goodsTotal: Double): Double
}

object ZeroCommissionStrategy : CommissionStrategy {
    override val policyName: String = "0%佣金 · 拒绝两头吃"
    override fun calculatePlatformFee(goodsTotal: Double): Double = 0.0
    override fun calculateTechFee(goodsTotal: Double): Double = 0.20
}

data class TransparentBillBreakdown(
    val goodsTotal: Double,
    val packagingTotal: Double,
    val riderFee: Double,
    val platformTechFee: Double,
    val platformCommissionFee: Double,
    val otherPlatformGoodsTotal: Double,
    val otherCommissionRate: Int
) {
    val userPayTotal: Double
        get() = round2(goodsTotal + packagingTotal + riderFee + platformTechFee + platformCommissionFee)
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
        items: List<Triple<Double, Double, Int>>,
        otherPrices: List<Pair<Double, Int>>,
        riderFee: Double,
        otherCommissionRate: Int = 23,
        strategy: CommissionStrategy = ZeroCommissionStrategy
    ): TransparentBillBreakdown {
        val goods = items.sumOf { it.first * it.third }
        val pack = items.sumOf { it.second * it.third }
        val otherGoods = otherPrices.sumOf { it.first * it.second }
        return TransparentBillBreakdown(
            goodsTotal = goods,
            packagingTotal = pack,
            riderFee = riderFee,
            platformTechFee = strategy.calculateTechFee(goods),
            platformCommissionFee = strategy.calculatePlatformFee(goods),
            otherPlatformGoodsTotal = otherGoods,
            otherCommissionRate = otherCommissionRate
        )
    }
}plication.domain.model

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
