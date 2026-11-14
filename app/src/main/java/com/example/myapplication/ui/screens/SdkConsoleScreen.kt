package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandOrange = Color(0xFFFF5722)
private val BrandYellow = Color(0xFFFFC107)
private val DarkSlate = Color(0xFF0F172A)

@Composable
fun SdkConsoleScreen(
    address: String,
    baseUrl: String,
    amapKey: String,
    sdkLogs: List<String>,
    onAddressChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onAmapKeyChange: (String) -> Unit,
    onSimulatePing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "⚙️ Retrofit / Room / 地图 / 支付 SDK 联调配置台",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Room 持久化收货地址 (重启App保留)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = onBaseUrlChange,
                    label = { Text("Retrofit 多租户后端 Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amapKey,
                    onValueChange = onAmapKeyChange,
                    label = { Text("高德地图 Android SDK AppKey") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = onSimulatePing,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("发送模拟多租户 Retrofit 请求并写入 Room 日志")
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "📟 实时 SDK 与数据层运行日志 (${sdkLogs.size})",
                    color = BrandYellow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                sdkLogs.take(12).forEach { log ->
                    Text("• $log", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }
        }
    }
}
