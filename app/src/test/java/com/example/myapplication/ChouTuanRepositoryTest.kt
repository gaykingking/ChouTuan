package com.example.myapplication

import com.example.myapplication.data.local.OrderRoomEntity
import com.example.myapplication.data.repository.ChouTuanRepository
import com.example.myapplication.data.repository.DataResult
import com.example.myapplication.di.InMemoryChouTuanRoomDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChouTuanRepositoryTest {

    @Test
    fun syncTenants_cachesTenantOfflineAndReturnsSuccessFromCache() = runBlocking {
        val dao = InMemoryChouTuanRoomDao()
        val repository = ChouTuanRepository(localDao = dao, remoteApi = null)

        val result = repository.syncTenants("tenant-001")

        assertTrue(result is DataResult.Success)
        val success = result as DataResult.Success
        assertTrue(success.fromCache)
        assertEquals(1, success.data.size)
        assertEquals("tenant-001", success.data.first().tenantId)
        assertTrue(success.data.first().isActive)

        val logs = repository.diagnosticsLogs.first()
        assertTrue(logs.any { it.contains("tenant-001") })
    }

    @Test
    fun cacheOrderWithTimeout_writesOrderWith15MinuteExpiry() = runBlocking {
        val dao = InMemoryChouTuanRoomDao()
        val repository = ChouTuanRepository(localDao = dao, remoteApi = null)

        val expiredOrder = OrderRoomEntity(
            orderId = "ord-expired",
            tenantId = "tenant-001",
            amount = 50.0,
            status = "PENDING",
            expireTimestampMillis = System.currentTimeMillis() - 60_000L
        )
        dao.insertOrder(expiredOrder)

        val before = System.currentTimeMillis()
        val activeOrder = OrderRoomEntity(
            orderId = "ord-active",
            tenantId = "tenant-001",
            amount = 128.5,
            status = "PENDING",
            expireTimestampMillis = 0L
        )
        repository.cacheOrderWithTimeout(activeOrder, timeoutMinutes = 15)
        val after = System.currentTimeMillis()

        val orders = dao.getOrdersByTenant("tenant-001")
        val savedActive = orders.first { it.orderId == "ord-active" }
        val expectedMinExpire = before + 15 * 60_000L
        val expectedMaxExpire = after + 15 * 60_000L
        assertTrue(savedActive.expireTimestampMillis in expectedMinExpire..expectedMaxExpire)

        val closedOrder = orders.first { it.orderId == "ord-expired" }
        assertTrue(closedOrder.status != "PENDING")

        val logs = repository.diagnosticsLogs.first()
        assertTrue(logs.any { it.contains("ord-active") && it.contains("15分钟") })
    }

    @Test
    fun appendLog_recordsDiagnosticsLogsAndRetainsLatest50() = runBlocking {
        val dao = InMemoryChouTuanRoomDao()
        val repository = ChouTuanRepository(localDao = dao, remoteApi = null)

        for (i in 1..55) {
            repository.appendLog("diag-log-$i")
        }

        val logs = repository.diagnosticsLogs.first()
        assertEquals(50, logs.size)
        assertEquals("diag-log-6", logs.first())
        assertEquals("diag-log-55", logs.last())
    }
}
