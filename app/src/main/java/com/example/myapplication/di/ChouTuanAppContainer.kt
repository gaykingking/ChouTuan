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

    override suspend fun getTenants(channel: String): List<TenantRoomEntity> {
        return if (channel == "全部") tenants.toList()
        else tenants.filter { it.channelCategory == channel }
    }

    override suspend fun upsertTenants(tenants: List<TenantRoomEntity>) {
        this.tenants.clear()
        this.tenants.addAll(tenants)
    }

    override suspend fun insertOrder(order: OrderRoomEntity) {
        orders.removeAll { it.orderId == order.orderId }
        orders.add(0, order)
    }

    override suspend fun updateOrderStatus(orderId: String, status: String) {
        val idx = orders.indexOfFirst { it.orderId == orderId }
        if (idx >= 0) {
            orders[idx] = orders[idx].copy(status = status)
        }
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
