package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class DailyRevenuePoint(
    val dayLabel: String,
    val grossRevenue: Double,
    val riderDeliveryFee: Double
) {
    val chouTuanNet: Double get() = grossRevenue
    val traditionalNet: Double get() = grossRevenue * 0.77
    val savedCommission: Double get() = grossRevenue * 0.23
}

@Composable
fun RevenueAnalyticsScreen(
    tenantName: String = "丑团示例餐饮店 (tenant_food_1)",
    dailyPoints: List<DailyRevenuePoint> = remember { defaultSevenDayRevenue() }
) {
    val totalNet = remember(dailyPoints) { dailyPoints.sumOf { it.chouTuanNet } }
    val totalTraditionalNet = remember(dailyPoints) { dailyPoints.sumOf { it.traditionalNet } }
    val totalSaved = remember(dailyPoints) { dailyPoints.sumOf { it.savedCommission } }
    val totalRiderFee = remember(dailyPoints) { dailyPoints.sumOf { it.riderDeliveryFee } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "0% 抽成收益对比报表",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "$tenantName · 近7日经营数据",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsSummaryCard(
                title = "累计实收 (丑团)",
                value = formatCurrency(totalNet),
                subtitle = "0% 平台抽成",
                accentColor = Color(0xFF16A34A),
                modifier = Modifier.weight(1f)
            )
            AnalyticsSummaryCard(
                title = "平台佣金",
                value = "¥0.00",
                subtitle = "永久免佣金承诺",
                accentColor = Color(0xFFFF5722),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsSummaryCard(
                title = "比传统平台多赚佣金",
                value = "+${formatCurrency(totalSaved)}",
                subtitle = "对比某团23%抽成",
                accentColor = Color(0xFF065F46),
                modifier = Modifier.weight(1f)
            )
            AnalyticsSummaryCard(
                title = "骑手全额运费",
                value = formatCurrency(totalRiderFee),
                subtitle = "0抽成直达骑手",
                accentColor = Color(0xFF2563EB),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "近7日实收对比 (丑团 0% vs 某团 23%)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF16A34A), label = "丑团 0% 抽成实收")
                    LegendItem(color = Color(0xFF94A3B8), label = "某团 23% 抽成实收")
                }

                SevenDayComparisonBarChart(
                    points = dailyPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Text(
                    text = "传统平台7日实收仅 ${formatCurrency(totalTraditionalNet)}，丑团帮您多留存 ${formatCurrency(totalSaved)} 利润。",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = Color(0xFF64748B))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF475569))
    }
}

@Composable
private fun SevenDayComparisonBarChart(
    points: List<DailyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    val maxVal = remember(points) {
        (points.maxOfOrNull { it.chouTuanNet } ?: 1.0).coerceAtLeast(1.0)
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            if (points.isEmpty()) return@Canvas

            val chartWidth = size.width
            val chartHeight = size.height
            val groupCount = points.size
            val groupWidth = chartWidth / groupCount
            val barWidth = (groupWidth * 0.28f).coerceAtLeast(8f)
            val barGap = (groupWidth * 0.08f).coerceAtLeast(4f)

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = chartHeight * (i.toFloat() / gridLines)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            points.forEachIndexed { index, point ->
                val centerX = groupWidth * index + groupWidth / 2f
                val chouTuanHeight = ((point.chouTuanNet / maxVal) * chartHeight).toFloat()
                val traditionalHeight = ((point.traditionalNet / maxVal) * chartHeight).toFloat()

                drawRoundRect(
                    color = Color(0xFF16A34A),
                    topLeft = Offset(
                        x = centerX - barWidth - barGap / 2f,
                        y = chartHeight - chouTuanHeight
                    ),
                    size = Size(width = barWidth, height = chouTuanHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                drawRoundRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(
                        x = centerX + barGap / 2f,
                        y = chartHeight - traditionalHeight
                    ),
                    size = Size(width = barWidth, height = traditionalHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            points.forEach { point ->
                Text(
                    text = point.dayLabel,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format(Locale.CHINA, "¥%.2f", amount)
}

private fun defaultSevenDayRevenue(): List<DailyRevenuePoint> = listOf(
    DailyRevenuePoint("D-6", 1820.0, 214.0),
    DailyRevenuePoint("D-5", 2150.0, 248.0),
    DailyRevenuePoint("D-4", 1960.0, 226.0),
    DailyRevenuePoint("D-3", 2480.0, 285.0),
    DailyRevenuePoint("D-2", 2790.0, 312.0),
    DailyRevenuePoint("D-1", 3120.0, 356.0),
    DailyRevenuePoint("今日", 2940.0, 334.0)
)
