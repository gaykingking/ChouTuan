package com.example.myapplication

import com.example.myapplication.ui.state.ChouTuanIntent
import com.example.myapplication.ui.viewmodel.ChouTuanViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChouTuanViewModelTest {

    @Test
    fun `onIntent SwitchRole updates activeRoleName and appends log`() {
        val viewModel = ChouTuanViewModel()
        viewModel.onIntent(ChouTuanIntent.SwitchRole("商家"))

        val state = viewModel.uiState.value
        assertEquals("商家", state.activeRoleName)
        assertTrue(state.sdkLogs.any { it.contains("[ViewModel] 切换角色 -> 商家") })
    }

    @Test
    fun `onIntent SwitchMerchantTenant updates activeTenantId and appends log`() {
        val viewModel = ChouTuanViewModel()
        viewModel.onIntent(ChouTuanIntent.SwitchMerchantTenant("merchant_102"))

        val state = viewModel.uiState.value
        assertEquals("merchant_102", state.activeTenantId)
        assertTrue(state.sdkLogs.any { it.contains("[ViewModel] 切换多租户商家 -> merchant_102") })
    }

    @Test
    fun `onIntent SelectChannel and UpdateSearchQuery update state fields`() {
        val viewModel = ChouTuanViewModel()
        viewModel.onIntent(ChouTuanIntent.SelectChannel("团购"))
        viewModel.onIntent(ChouTuanIntent.UpdateSearchQuery("轻食"))

        val state = viewModel.uiState.value
        assertEquals("团购", state.selectedChannel)
        assertEquals("轻食", state.searchQuery)
    }

    @Test
    fun `onIntent UpdateAddress updates deliveryAddress and appends log`() {
        val viewModel = ChouTuanViewModel()
        val targetAddress = "静安区南京西路88号"
        viewModel.onIntent(ChouTuanIntent.UpdateAddress(targetAddress))

        val state = viewModel.uiState.value
        assertEquals(targetAddress, state.deliveryAddress)
        assertTrue(state.sdkLogs.any { it.contains("[ViewModel] 收货地址已更新 -> $targetAddress") })
    }

    @Test
    fun `onIntent RedeemVoucher appends verification log`() {
        val viewModel = ChouTuanViewModel()
        viewModel.onIntent(ChouTuanIntent.RedeemVoucher("CT-2026-888"))

        val state = viewModel.uiState.value
        assertTrue(state.sdkLogs.any { it.contains("[ViewModel] 发起团购券核销校验 -> CT-2026-888") })
    }

    @Test
    fun `previewTransparentBill calculates zero commission breakdown and updates state`() {
        val viewModel = ChouTuanViewModel()
        val breakdown = viewModel.previewTransparentBill(
            items = listOf(Triple(30.0, 2.0, 1)),
            otherPrices = listOf(Pair(40.0, 1)),
            riderFee = 4.0,
            otherCommissionRate = 23
        )

        assertEquals(0.0, breakdown.platformCommissionFee, 0.001)
        assertEquals(0.20, breakdown.platformTechFee, 0.001)
        assertEquals(31.80, breakdown.merchantNetIncome, 0.001)
        assertEquals(4.0, breakdown.riderNetIncome, 0.001)
        assertEquals(36.0, breakdown.userPayTotal, 0.001)
        assertEquals(8.0, breakdown.userSavedAmount, 0.001)

        val currentPreview = viewModel.uiState.value.currentBillPreview
        assertNotNull(currentPreview)
        assertEquals(breakdown, currentPreview)
    }
}
