package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ChouTuanRoomDao
import com.example.myapplication.data.local.OrderRoomEntity
import com.example.myapplication.data.local.TenantRoomEntity
import com.example.myapplication.data.remote.ChouTuanApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface DataResult<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : DataResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : DataResult<Nothing>
}

class ChouTuanRepository(
    private val localDao: ChouTuanRoomDao?,
    private val remoteApi: ChouTuanApiService?
) {
    private val _diagnosticsLogs = MutableStateFlow(
        listOf("[Repository] 已初始化 SOLID 离线优先多租户仓库")
    )
    val diagnosticsLogs: Flow<List<String>> = _diagnosticsLogs.asStateFlow()

    suspend fun syncTenants(channel: String): DataResult<List<TenantRoomEntity>> {
        return try {
            val defaultTenant = TenantRoomEntity(
                tenantId = channel,
                tenantName = channel,
                isActive = true
            )
            localDao?.insertTenant(defaultTenant)
            val cachedOrders = localDao?.getOrdersByTenant(channel).orEmpty()
            val cached = listOf(defaultTenant)
            appendLog("[Repository] 已同步租户 ${defaultTenant.tenantId} 至 Room，关联订单 ${cachedOrders.size} 条，频道=$channel")
            DataResult.Success(cached, fromCache = true)
        } catch (e: Exception) {
            appendLog("[Repository] 同步租户异常: ${e.message}")
            DataResult.Error("加载租户失败: ${e.message}", e)
        }
    }

    suspend fun cacheOrderWithTimeout(order: OrderRoomEntity, timeoutMinutes: Int = 15) {
        val now = System.currentTimeMillis()
        val expireAt = now + timeoutMinutes * 60_000L
        localDao?.insertOrder(order.copy(expireTimestampMillis = expireAt))
        localDao?.closeExpiredOrders(now)
        appendLog("[Repository] 订单 ${order.orderId} 已写入 Room，超时关单=${timeoutMinutes}分钟")
    }

    fun appendLog(msg: String) {
        _diagnosticsLogs.value = (_diagnosticsLogs.value + msg).takeLast(50)
    }
}
