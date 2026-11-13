package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val BrandOrange = Color(0xFFFF5722)
private val BrandYellow = Color(0xFFFFC107)
private val FairGreen = Color(0xFF16A34A)
private val RiderBlue = Color(0xFF1E3A8A)

data class RiderTaskCardModel(
    val orderId: String,
    val tenantName: String,
    val tenantEmoji: String,
    val tenantTypeLabel: String,
    val deliveryAddress: String,
    val riderNetIncome: Double,
    val statusLabel: String,
    val isPendingPickup: Boolean,
    val isDelivering: Boolean,
    val isCompleted: Boolean
)

/**
 * 独立拆分的骑手端大厅 Screen：只依赖轻量不可变 RiderTaskCardModel，彻底解耦底层数据模型
 */
@Composable
fun RiderDeliveryScreen(
    tasks: List<RiderTaskCardModel>,
    onPickupOrder: (String) -> Unit,
    onCompleteDelivery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalEarned = tasks.filter { it.isCompleted }.sumOf { it.riderNetIncome }
    val earnedRounded = (totalEarned * 100.0).roundToInt() / 100.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RiderBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        "🛵 丑团骑手大厅 · 配送费100%归骑手",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "支持外卖/闪购/超市/买药全品类接单，预留安全红绿灯缓冲时间，超时免罚款。",
                        color = Color(0xFFBFDBFE),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "今日已结算配送收入：¥$earnedRounded (100%全额到账)",
                        color = BrandYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        items(tasks, key = { it.orderId }) { task ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${task.tenantEmoji} 取货：${task.tenantName}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "运费 ¥${task.riderNetIncome} 全得",
                            color = FairGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "送达：${task.deliveryAddress} · 业态：${task.tenantTypeLabel}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        "当前状态：${task.statusLabel}",
                        fontSize = 12.sp,
                        color = BrandOrange
                    )
                    Spacer(Modifier.height(8.dp))
                    when {
                        task.isPendingPickup -> {
                            Button(
                                onClick = { onPickupOrder(task.orderId) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                            ) {
                                Text("抢单并确认到店取货")
                            }
                        }
                        task.isDelivering -> {
                            Button(
                                onClick = { onCompleteDelivery(task.orderId) },
                                colors = ButtonDefaults.buttonColors(containerColor = FairGreen)
                            ) {
                                Text("确认安全送达（秒到账 ¥${task.riderNetIncome}）")
                            }
                        }
                        else -> {
                            Text(
                                "✅ 已送达 · 配送费 ¥${task.riderNetIncome} 已100%入账",
                                color = FairGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
