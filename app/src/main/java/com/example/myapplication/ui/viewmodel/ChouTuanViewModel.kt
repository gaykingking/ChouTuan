package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.local.OrderRoomEntity
import com.example.myapplication.data.repository.ChouTuanRepository
import com.example.myapplication.domain.model.CommissionStrategy
import com.example.myapplication.domain.model.TransparentBillBreakdown
import com.example.myapplication.domain.model.TransparentPricingEngine
import com.example.myapplication.domain.model.ZeroCommissionStrategy
import com.example.myapplication.ui.state.ChouTuanIntent
import com.example.myapplication.ui.state.ChouTuanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * SOLID - 单一职责 & 依赖倒置
 * 统一调度 UI 意图 (ChouTuanIntent)，协调 Repository 离线缓存与 TransparentPricingEngine 透明计价。
 */
class ChouTuanViewModel(
    private val repository: ChouTuanRepository = ChouTuanRepository(null, null),
    private val commissionStrategy: CommissionStrategy = ZeroCommissionStrategy
) {
    private val _uiState = MutableStateFlow(ChouTuanUiState())
    val uiState: StateFlow<ChouTuanUiState> = _uiState.asStateFlow()

    fun onIntent(intent: ChouTuanIntent) {
        when (intent) {
            is ChouTuanIntent.SwitchRole -> {
                _uiState.update { it.copy(activeRoleName = intent.roleName) }
                appendSdkLog("[ViewModel] 切换角色 -> ${intent.roleName}")
            }
            is ChouTuanIntent.SwitchMerchantTenant -> {
                _uiState.update { it.copy(activeTenantId = intent.tenantId) }
                appendSdkLog("[ViewModel] 切换多租户商家 -> ${intent.tenantId}")
            }
            is ChouTuanIntent.SelectChannel -> {
                _uiState.update { it.copy(selectedChannel = intent.channel) }
            }
            is ChouTuanIntent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
            }
            is ChouTuanIntent.UpdateAddress -> {
                _uiState.update { it.copy(deliveryAddress = intent.address) }
                appendSdkLog("[ViewModel] 收货地址已更新 -> ${intent.address}")
            }
            is ChouTuanIntent.RedeemVoucher -> {
                appendSdkLog("[ViewModel] 发起团购券核销校验 -> ${intent.code}")
            }
        }
    }

    fun previewTransparentBill(
        items: List<Triple<Double, Double, Int>>,
        otherPrices: List<Pair<Double, Int>>,
        riderFee: Double,
        otherCommissionRate: Int = 23
    ): TransparentBillBreakdown {
        val breakdown = TransparentPricingEngine.calculateBill(
            items = items,
            otherPrices = otherPrices,
            riderFee = riderFee,
            otherCommissionRate = otherCommissionRate,
            strategy = commissionStrategy
        )
        _uiState.update { it.copy(currentBillPreview = breakdown) }
        return breakdown
    }

    suspend fun recordOrderCache(order: OrderRoomEntity) {
        repository.cacheOrderWithTimeout(order, timeoutMinutes = 15)
        appendSdkLog("[ViewModel] 已记录订单 ${order.orderId} 至本地15分钟超时监控队列")
    }

    private fun appendSdkLog(msg: String) {
        repository.appendLog(msg)
        _uiState.update { state ->
            state.copy(sdkLogs = (listOf(msg) + state.sdkLogs).take(30))
        }
    }
}
