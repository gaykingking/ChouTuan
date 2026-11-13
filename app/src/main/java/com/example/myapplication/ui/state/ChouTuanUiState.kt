package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.TransparentBillBreakdown

data class ChouTuanUiState(
    val activeRoleName: String = "USER",
    val activeTenantId: String = "tenant_food_1",
    val selectedChannel: String = "全部",
    val searchQuery: String = "",
    val deliveryAddress: String = "大兴观音寺街道·首座御园 3号楼 1202室",
    val currentBillPreview: TransparentBillBreakdown? = null,
    val isOfflineFallbackMode: Boolean = true,
    val sdkLogs: List<String> = emptyList()
)

sealed interface ChouTuanIntent {
    data class SwitchRole(val roleName: String) : ChouTuanIntent
    data class SwitchMerchantTenant(val tenantId: String) : ChouTuanIntent
    data class SelectChannel(val channel: String) : ChouTuanIntent
    data class UpdateSearchQuery(val query: String) : ChouTuanIntent
    data class UpdateAddress(val address: String) : ChouTuanIntent
    data class RedeemVoucher(val code: String) : ChouTuanIntent
}
