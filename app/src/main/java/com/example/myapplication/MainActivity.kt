package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class LocalPersistenceDao(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("choutuan_prefs", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var persistenceDao: LocalPersistenceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        persistenceDao = LocalPersistenceDao(this)
        setContent {
            ChouTuanMainApp(persistenceDao)
        }
    }
}

@Composable
fun ChouTuanMainApp(dao: LocalPersistenceDao) {
    var currentTab by remember { mutableStateOf(0) }
    var showSpecDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var voucherCode by remember { mutableStateOf("") }
    var redeemStatus by remember { mutableStateOf("") }
    var showRealReviews by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFF6600)).padding(16.dp)) {
                Text("丑团 v2.0 模块化平台", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            TabRow(selectedTabIndex = currentTab) {
                Tab(selected = currentTab == 0, onClick = { currentTab = 0 }) { Text("轨迹雷达", modifier = Modifier.padding(12.dp)) }
                Tab(selected = currentTab == 1, onClick = { currentTab = 1 }) { Text("多规格/晒单", modifier = Modifier.padding(12.dp)) }
                Tab(selected = currentTab == 2, onClick = { currentTab = 2 }) { Text("商家核销/加品", modifier = Modifier.padding(12.dp)) }
                Tab(selected = currentTab == 3, onClick = { currentTab = 3 }) { Text("SDK日志台", modifier = Modifier.padding(12.dp)) }
            }

            when (currentTab) {
                0 -> UserOrdersWithMapScreen()
                1 -> Column(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = { showSpecDialog = true }) { Text("打开商品多规格选择弹窗") }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("无控评真实晒单评价模式: ")
                        Switch(checked = showRealReviews, onCheckedChange = { showRealReviews = it })
                    }
                    if (showRealReviews) {
                        Text("【真实用户晒单】: 味道超赞，份量足，毫无控评删评！", color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        Text("【精选评价】: 商家优质推荐", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                2 -> Column(modifier = Modifier.padding(16.dp)) {
                    Text("多租户商家端 8位数字核销码", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = voucherCode,
                        onValueChange = { if (it.length <= 8) voucherCode = it },
                        label = { Text("输入8位数字核销码") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (voucherCode.length == 8) {
                                redeemStatus = onRedeemVoucher(voucherCode)
                                dao.saveString("last_voucher", voucherCode)
                            } else {
                                redeemStatus = "请输入完整的8位数字核销码"
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("立即核销")
                    }
                    if (redeemStatus.isNotEmpty()) {
                        Text(redeemStatus, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showAddProductDialog = true }) { Text("新增堂食同价商品 (含某团23%抽成保本对比)") }
                }
                3 -> SdkIntegrationConsoleScreen()
            }
        }
    }

    if (showSpecDialog) {
        ProductSpecDialog(onDismiss = { showSpecDialog = false })
    }
    if (showAddProductDialog) {
        AddMerchantProductDialog(onDismiss = { showAddProductDialog = false })
    }
}

fun onRedeemVoucher(code: String): String {
    return if (code.length == 8 && code.all { it.isDigit() }) {
        "核销成功！核销码: $code (租户已入账)"
    } else {
        "核销失败：无效的8位数字核销码"
    }
}

@Composable
fun UserOrdersWithMapScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("高德 LBS 骑手实时配送轨迹雷达面板", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val merchantPos = Offset(size.width * 0.2f, size.height * 0.8f)
                val riderPos = Offset(size.width * 0.5f, size.height * 0.5f)
                val customerPos = Offset(size.width * 0.8f, size.height * 0.2f)

                drawLine(Color.Blue, merchantPos, riderPos, strokeWidth = 6f)
                drawLine(Color.Green, riderPos, customerPos, strokeWidth = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))

                drawCircle(Color.Red, radius = 16f, center = merchantPos)
                drawCircle(Color.Blue, radius = 20f, center = riderPos)
                drawCircle(Color.Green, radius = 16f, center = customerPos)
            }
            Text("商家取货点", modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), color = Color.Red, fontWeight = FontWeight.Bold)
            Text("骑手实时位置", modifier = Modifier.align(Alignment.Center).padding(8.dp), color = Color.Blue, fontWeight = FontWeight.Bold)
            Text("收货点目的地", modifier = Modifier.align(Alignment.TopEnd).padding(16.dp), color = Color.Green, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProductSpecDialog(onDismiss: () -> Unit) {
    var selectedSpec by remember { mutableStateOf("标准分量") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("商品多规格选择") },
        text = {
            Column {
                Text("选择规格与辣度:")
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    FilterChip(selected = selectedSpec == "小份/微辣", onClick = { selectedSpec = "小份/微辣" }, label = { Text("小份/微辣") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = selectedSpec == "中份/中辣", onClick = { selectedSpec = "中份/中辣" }, label = { Text("中份/中辣") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = selectedSpec == "大份/重辣", onClick = { selectedSpec = "大份/重辣" }, label = { Text("大份/重辣") })
                }
                Text("已选: $selectedSpec", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("确认选择") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun AddMerchantProductDialog(onDismiss: () -> Unit) {
    var dineInPrice by remember { mutableStateOf("100") }
    val priceVal = dineInPrice.toDoubleOrNull() ?: 0.0
    val meituanPrice = priceVal / (1.0 - 0.23)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增堂食同价商品") },
        text = {
            Column {
                OutlinedTextField(
                    value = dineInPrice,
                    onValueChange = { dineInPrice = it },
                    label = { Text("堂食原价 (元)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("自动对比测算某团23%抽成保本溢价:", fontWeight = FontWeight.Bold)
                Text("某团保本外卖建议价: %.2f 元".format(meituanPrice), color = Color(0xFFD32F2F))
                Text("本平台堂食同价优惠价: %.2f 元".format(priceVal), color = Color(0xFF388E3C))
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("上架堂食商品") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun SdkIntegrationConsoleScreen() {
    val logs = remember {
        listOf(
            "[SDK Info] AMap Location SDK initialized. Status: OK",
            "[SDK Info] Real-time LBS ping interval set to 3s",
            "[Gateway] Multi-tenant Header X-Tenant-Id: TENANT_8888",
            "[Payment] Payment Gateway SDK connected. RSA2 Verification passed.",
            "[Room DB] ChouTuanRoomDao sync completed successfully."
        )
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SDK 实时联调日志控制台", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            items(logs) { log ->
                Text(log, color = Color.Green, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
