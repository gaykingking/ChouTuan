package com.example.myapplication.di

import com.example.myapplication.data.local.ChouTuanRoomDao
import com.example.myapplication.data.local.OrderRoomEntity
import com.example.myapplication.data.local.TenantRoomEntity
import com.example.myapplication.data.remote.ChouTuanApiService
import com.example.myapplication.data.repository.ChouTuanRepository
import com.example.myapplication.domain.model.CommissionStrategy
import com.example.myapplication.domain.model.ZeroCommissionStrategy
import com.example.myapplication.ui.viewmodel.ChouTuanViewModel

class InMemoryChouTuanRoomDao : ChouTuanRoomDao {
    private val tenants = mutableListOf<TenantRoomEntity>()
    private val orders = mutableListOf<OrderRoomEntity>()

    override suspend fun insertTenant(tenant: TenantRoomEntity) {
        tenants.removeAll { it.tenantId == tenant.tenantId }
        tenants.add(tenant)
    }

    override suspend fun insertOrder(order: OrderRoomEntity) {
        orders.removeAll { it.orderId == order.orderId }
        orders.add(0, order)
    }

    override suspend fun getOrdersByTenant(tenantId: String): List<OrderRoomEntity> {
        return orders.filter { it.tenantId == tenantId }
    }

    override suspend fun closeExpiredOrders(currentTimeMillis: Long): Int {
        var count = 0
        for (i in orders.indices) {
            val order = orders[i]
            if (order.expireTimestampMillis <= currentTimeMillis && order.status != "EXPIRED_CLOSED") {
                orders[i] = order.copy(status = "EXPIRED_CLOSED")
                count++
            }
        }
        return count
    }
}

class ChouTuanAppContainer(
    val roomDao: ChouTuanRoomDao = InMemoryChouTuanRoomDao(),
    val remoteApi: ChouTuanApiService? = null,
    val commissionStrategy: CommissionStrategy = ZeroCommissionStrategy
) {
    val repository: ChouTuanRepository by lazy {
        ChouTuanRepository(localDao = roomDao, remoteApi = remoteApi)
    }

    fun createViewModel(): ChouTuanViewModel {
        return ChouTuanViewModel(
            repository = repository,
            commissionStrategy = commissionStrategy
        )
    }
}
