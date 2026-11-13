package com.example.myapplication

import com.example.myapplication.domain.model.TransparentPricingEngine
import com.example.myapplication.domain.model.ZeroCommissionStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransparentPricingEngineTest {

    @Test
    fun `zero commission bill gives 100 percent food and pack to merchant and full fee to rider`() {
        val bill = TransparentPricingEngine.calculateBill(
            items = listOf(
                Triple(18.0, 1.0, 1), // 辣椒炒肉木桶饭 18元 + 打包1元
                Triple(6.0, 0.0, 1)   // 卤鸡腿 6元
            ),
            otherPrices = listOf(
                25.8 to 1,
                9.9 to 1
            ),
            riderFee = 3.5,
            otherCommissionRate = 23,
            strategy = ZeroCommissionStrategy
        )

        assertEquals(0.0, bill.platformCommissionFee, 0.001)
        assertEquals(0.20, bill.platformTechFee, 0.001)
        assertEquals(25.0, bill.merchantNetIncome, 0.001)
        assertEquals(3.5, bill.riderNetIncome, 0.001)
        assertEquals(28.7, bill.userPayTotal, 0.001)
        assertEquals(11.7, bill.userSavedAmount, 0.001)
        assertTrue(bill.merchantSavedCommission > 5.0)
    }
}
