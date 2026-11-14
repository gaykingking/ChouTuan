package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val BrandOrange = Color(0xFFFF5722)
private val FairGreen = Color(0xFF16A34A)
private val FairGreenBg = Color(0xFFDCFCE7)
private val DarkSlate = Color(0xFF0F172A)

data class HomeTenantCardModel(
    val tenantId: String,
    val name: String,
    val emoji: String,
    val channelCategory: String,
    val rating: Double,
    val monthlySales: Int,
    val deliveryTimeMinutes: Int,
    val distanceKm: Double,
    val riderFee: Double,
    val otherCommissionRate: Int,
    val avgSavedPerDish: Double
)

@Composable
fun UserChannelHomeScreen(
    tenants: List<HomeTenantCardModel>,
    address: String,
    searchQuery: String,
    selectedChannel: String,
    onSearchChange: (String) -> Unit,
    onChannelSelect: (String) -> Unit,
    onShopClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val channels = listOf(
        "全部" to "🔥",
        "快餐便当" to "🍱",
        "丑团闪购" to "⚡",
        "生鲜超市" to "🥬",
        "到店团购" to "🎫",
        "平价买药" to "💊",
        "米粉面馆" to "🍜"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📍 $address", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                    Surface(color = FairGreenBg, shape = RoundedCornerShape(99.dp)) {
                        Text(
                            "🛡️ 全频道0抽成 · 无杀熟",
                            color = FairGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("搜外卖、闪购、超市、买药（辣椒炒肉、啤酒、空心菜）", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(channels) { (ch, icon) ->
                    val selected = selectedChannel == ch
                    Surface(
                        color = if (selected) BrandOrange else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { onChannelSelect(ch) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, fontSize = 15.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                ch,
                                color = if (selected) Color.White else DarkSlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        items(tenants, key = { it.tenantId }) { tenant ->
            val avgSavedRounded = (tenant.avgSavedPerDish * 10.0).roundToInt() / 10.0
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clickable { onShopClick(tenant.tenantId) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                        ) {
                            Text(tenant.emoji, fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    tenant.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = DarkSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(color = FairGreenBg, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        "${tenant.channelCategory}·0抽成",
                                        color = FairGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "⭐ ${tenant.rating} | 月售 ${tenant.monthlySales} | ${tenant.deliveryTimeMinutes}分钟 · ${tenant.distanceKm}km | 运费 ¥${tenant.riderFee}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(Modifier.height(5.dp))
                            Surface(color = Color(0xFFFEF2F2), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "💡 某团抽佣${tenant.otherCommissionRate}%被迫涨价 → 丑团单品均省 ¥$avgSavedRounded",
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
