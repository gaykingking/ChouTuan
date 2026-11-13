package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.TransparentBillBreakdown

private val FairGreen = Color(0xFF16A34A)
private val FairGreenBg = Color(0xFFF0FDF4)
private val DarkSlate = Color(0xFF0F172A)

/**
 * 高内聚可复用组件：实时渲染“丑团 0% 抽成”资金分账明细卡片
 */
@Composable
fun TransparentBillCard(
    breakdown: TransparentBillBreakdown,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color = FairGreenBg,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "💰 本单资金流向 100% 透明公示：",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = FairGreen
                )
                BillLine("🏪 商家租户实收（货款+打包）", "¥${breakdown.merchantNetIncome} (100%到账)")
                BillLine("🛵 骑手实得（全额配送费）", "¥${breakdown.riderNetIncome} (0平台克扣)")
                BillLine("⚙️ 支付通道与服务器成本", "¥${breakdown.platformTechFee} (固定成本)")
                BillLine("🚫 丑团平台佣金抽成", "¥${breakdown.platformCommissionFee} (0%)")
            }
        }
        Surface(
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "🔥 相比某团同款，本单为您省下 ¥${breakdown.userSavedAmount}，同时帮商家少交约 ¥${breakdown.merchantSavedCommission} 平台佣金！",
                color = Color(0xFFDC2626),
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun BillLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = DarkSlate)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FairGreen)
    }
}
