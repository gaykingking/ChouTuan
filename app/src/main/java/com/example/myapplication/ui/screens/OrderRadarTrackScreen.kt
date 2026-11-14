package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val FairGreen = Color(0xFF16A34A)
private val DarkSlate = Color(0xFF0F172A)

data class OrderRadarCardModel(
    val orderId: String,
    val tenantName: String,
    val statusLabel: String,
    val progress: Float,
    val shopLatLabel: String,
    val riderPositionLabel: String,
    val userLatLabel: String,
    val paymentChannel: String,
    val userPayTotal: Double,
    val merchantNetIncome: Double,
    val riderNetIncome: Double,
    val platformTechFee: Double,
    val isCompleted: Boolean
)

@Composable
fun OrderRadarTrackScreen(
    orders: List<OrderRadarCardModel>,
    onAdvanceStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "🛵 订单追踪与高德 LBS 实时轨迹",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = DarkSlate
            )
        }
        items(orders, key = { it.orderId }) { order ->
            val totalRounded = (order.userPayTotal * 10.0).roundToInt() / 10.0
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${order.tenantName} (${order.orderId})", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(order.statusLabel, color = FairGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFFE0F2FE), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val start = Offset(40f, size.height * 0.65f)
                            val end = Offset(size.width - 40f, size.height * 0.35f)
                            val riderX = start.x + (end.x - start.x) * order.progress
                            val riderY = start.y + (end.y - start.y) * order.progress

                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = start,
                                end = end,
                                strokeWidth = 6f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                            drawLine(
                                color = Color(0xFF16A34A),
                                start = start,
                                end = Offset(riderX, riderY),
                                strokeWidth = 8f
                            )
                            drawCircle(color = Color(0xFFEA580C), radius = 14f, center = start)
                            drawCircle(color = Color(0xFF2563EB), radius = 14f, center = end)
                            drawCircle(color = Color(0xFF16A34A), radius = 18f, center = Offset(riderX, riderY))
                        }
                        Text("🏪 商家(${order.shopLatLabel})", fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomStart))
                        Text("🛵 ${order.riderPositionLabel}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = FairGreen, modifier = Modifier.align(Alignment.TopCenter))
                        Text("🏠 收货点(${order.userLatLabel})", fontSize = 10.sp, modifier = Modifier.align(Alignment.TopEnd))
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "💳 ${order.paymentChannel} | 实付 ¥$totalRounded = 商家实收 ¥${order.merchantNetIncome} + 骑手实得 ¥${order.riderNetIncome} + 通道费 ¥${order.platformTechFee}",
                        fontSize = 11.sp,
                        color = FairGreen,
                        fontWeight = FontWeight.Bold
                    )
                    if (!order.isCompleted) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onAdvanceStatus(order.orderId) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("模拟推进配送轨迹", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
