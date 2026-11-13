package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "tenants")
data class TenantRoomEntity(
    @PrimaryKey val tenantId: String,
    val tenantName: String,
    val isActive: Boolean = true
)

@Entity(tableName = "orders")
data class OrderRoomEntity(
    @PrimaryKey val orderId: String,
    val tenantId: String,
    val amount: Double,
    val status: String,
    val expireTimestampMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ChouTuanRoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderRoomEntity)

    @Query("SELECT * FROM orders WHERE tenantId = :tenantId")
    suspend fun getOrdersByTenant(tenantId: String): List<OrderRoomEntity>

    @Query("UPDATE orders SET status = 'EXPIRED_CLOSED' WHERE expireTimestampMillis <= :currentTimeMillis AND status = 'PENDING'")
    suspend fun closeExpiredOrders(currentTimeMillis: Long = System.currentTimeMillis()): Int
}

@Database(entities = [TenantRoomEntity::class, OrderRoomEntity::class], version = 1, exportSchema = false)
abstract class ChouTuanDatabase : RoomDatabase() {
    abstract fun chouTuanRoomDao(): ChouTuanRoomDao
}
