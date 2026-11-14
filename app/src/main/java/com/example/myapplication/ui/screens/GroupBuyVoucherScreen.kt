package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandOrange = Color(0xFFFF5722)
private val BrandYellow = Color(0xFFFFC107)
private val FairGreen = Color(0xFF16A34A)
private val FairGreenBg = Color(0xFFDCFCE7)
private val DarkSlate = Color(0xFF0F172A)

data class GroupVoucherCardModel(
    val code: String,
    val dealTitle: String,
    val shopName: String,
    val price: Double,
    val redeemed: Boolean
)

data class GroupDealCardModel(
    val id: String,
    val shopName: String,
    val title: String,
    val storeOriginalValue: Double,
    val choutuanPrice: Double,
    val otherPlatformPrice: Double,
    val emoji: String
)

@Composable
fun GroupBuyVoucherScreen(
    deals: List<GroupDealCardModel>,
    vouchers: List<GroupVoucherCardModel>,
    onBuyDeal: (GroupDealCardModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlate),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "🎫 丑团到店团购 · 0抽成真直降",
                        color = BrandYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "购买后生成8位数字核销码，可切换至【商家端】输入核销码体验秒级分账到店！",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
        items(vouchers, key = { it.code }) { v ->
            Card(
                colors = CardDefaults.cardColors(containerColor = FairGreenBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🎟️ ${v.dealTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "8位核销码：${v.code} · 金额 ¥${v.price}",
                            fontSize = 12.sp,
                            color = FairGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        if (v.redeemed) "✅ 已核销" else "待到店核销",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (v.redeemed) Color.Gray else BrandOrange
                    )
                }
            }
        }
        items(deals, key = { it.id }) { deal ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(deal.emoji, fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(deal.shopName, fontSize = 11.sp, color = Color.Gray)
                        Text(deal.title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(
                            "丑团价 ¥${deal.choutuanPrice}  (某团¥${deal.otherPlatformPrice} / 门市¥${deal.storeOriginalValue})",
                            color = BrandOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = { onBuyDeal(deal) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("抢购")
                    }
                }
            }
        }
    }
}
