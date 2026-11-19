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
