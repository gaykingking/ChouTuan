package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val BrandOrange = Color(0xFFFF5722)
private val BrandYellow = Color(0xFFFFC107)
private val FairGreen = Color(0xFF16A34A)
private val MerchantDarkGreen = Color(0xFF065F46)
private val DarkSlate = Color(0xFF0F172A)

data class MerchantTenantOptionModel(
    val tenantId: String,
    val name: String,
    val emoji: String,
    val tenantTypeLabel: String,
    val otherCommissionRate: Int
)

data class MerchantOrderCardModel(
    val orderId: String,
    val merchantNetIncome: Double,
    val statusLabel: String,
    val isPendingAccept: Boolean
)

data class MerchantProductCardModel(
    val productId: String,
    val name: String,
    val emoji: String,
    val storePrice: Double,
    val otherPlatformPrice: Double,
    val isAvailable: Boolean
)

/**
 * 独立拆分的多租户商家端工作台 Screen：
 * 支持餐饮、闪购、生鲜超市、平价药房多租户切换、8位团购码核销、0抽成账本与同价商品管理。
 */
@Composable
fun MerchantWorkspaceScreen(
    tenants: List<MerchantTenantOptionModel>,
    activeTenantId: String,
    orders: List<MerchantOrderCardModel>,
    products: List<MerchantProductCardModel>,
    onSelectTenant: (String) -> Unit,
    onRedeemVoucher: (String) -> Unit,
    onOpenAddProduct: () -> Unit,
    onToggleProductAvailability: (String) -> Unit,
    onAcceptOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTenant = tenants.find { it.tenantId == activeTenantId } ?: tenants.firstOrNull()
    val totalNetRevenue = orders.sumOf { it.merchantNetIncome }
    val revenueRounded = (totalNetRevenue * 10.0).roundToInt() / 10.0
    val savedCommission = ((totalNetRevenue * ((activeTenant?.otherCommissionRate ?: 23) / 100.0)) * 10.0).roundToInt() / 10.0
    var voucherCodeInput by remember { mutableStateOf("88291042") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        "🏢 多租户商家切换器（餐饮/闪购/超市/买药多租户隔离）",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = DarkSlate
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tenants, key = { it.tenantId }) { t ->
                            FilterChip(
                                selected = t.tenantId == activeTenantId,
                                onClick = { onSelectTenant(t.tenantId) },
                                label = { Text("${t.emoji} ${t.name}") }
                            )
                        }
                    }
                }
            }
        }

        activeTenant?.let { tenant ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MerchantDarkGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(
                            "${tenant.emoji} ${tenant.name} · 0%抽成独立账本",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("本租户实收(100%)", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                                Text("¥$revenueRounded", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Column {
                                Text("丑团平台佣金", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                                Text("¥0.00 (0%)", color = BrandYellow, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Column {
                                Text("比某团少交抽成", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                                Text("+¥$savedCommission", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("🎟️ 到店团购券 0%抽成核销器", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = voucherCodeInput,
                            onValueChange = { voucherCodeInput = it },
                            label = { Text("输入8位核销码 (如 88291042)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onRedeemVoucher(voucherCodeInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = FairGreen)
                        ) {
                            Text("立即核销")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📦 本租户门店同价商品 (${products.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = DarkSlate
                )
                Button(
                    onClick = onOpenAddProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("+ 新增同价商品", fontSize = 12.sp)
                }
            }
        }

        items(products, key = { it.productId }) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${item.emoji} ${item.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "丑团门店价：¥${item.storePrice} | 某团溢价：¥${item.otherPlatformPrice}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    OutlinedButton(onClick = { onToggleProductAvailability(item.productId) }) {
                        Text(if (item.isAvailable) "在售·估清" else "已估清·上架", fontSize = 11.sp)
                    }
                }
            }
        }

        items(orders, key = { it.orderId }) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("订单 ${order.orderId} · 净收 ¥${order.merchantNetIncome}", fontWeight = FontWeight.Bold)
                        Text(order.statusLabel, fontSize = 12.sp, color = BrandOrange)
                    }
                    if (order.isPendingAccept) {
                        Button(
                            onClick = { onAcceptOrder(order.orderId) },
                            colors = ButtonDefaults.buttonColors(containerColor = FairGreen)
                        ) {
                            Text("0抽成接单")
                        }
                    }
                }
            }
        }
    }
}
