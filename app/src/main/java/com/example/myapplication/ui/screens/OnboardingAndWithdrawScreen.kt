package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class OnboardingWithdrawTab(val title: String) {
    MERCHANT_ONBOARDING("0%抽成商家入驻"),
    INSTANT_WITHDRAW("0手续费秒级提现")
}

enum class WithdrawRole(val label: String, val defaultBalance: String) {
    MERCHANT("商家账户", "12850.00"),
    RIDER("骑手账户", "1640.50")
}

enum class WithdrawChannel(val label: String, val arrivalHint: String) {
    WECHAT_CHANGE("微信零钱", "秒级到账 · 0手续费"),
    ALIPAY("支付宝", "秒级到账 · 0手续费"),
    BANK_CARD("银行卡", "秒级到账 · 0手续费")
}

data class WithdrawBillRecord(
    val id: String,
    val roleLabel: String,
    val channel: String,
    val amount: String,
    val fee: String,
    val status: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingAndWithdrawScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(OnboardingWithdrawTab.MERCHANT_ONBOARDING) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商家入驻与秒级提现中心", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                OnboardingWithdrawTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                OnboardingWithdrawTab.MERCHANT_ONBOARDING -> MerchantOnboardingPanel()
                OnboardingWithdrawTab.INSTANT_WITHDRAW -> InstantWithdrawPanel()
            }
        }
    }
}

@Composable
private fun MerchantOnboardingPanel() {
    var tenantCode by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("中式快餐 / 堂食同价") }
    var contactPhone by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var agreedCovenant by remember { mutableStateOf(false) }
    var submitMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "多租户商家 0% 抽成自助入驻",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "平台承诺永久 0% 交易抽成、0 入驻年费，支持独立多租户门店编码快速开通。",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = tenantCode,
                onValueChange = { tenantCode = it },
                label = { Text("多租户品牌/门店编码（如 TENANT-1008）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("门店名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("主营品类") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("负责人手机号") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text("营业执照 / 统一社会信用代码") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                border = BorderStroke(1.dp, Color(0xFFFFB300)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "《100%堂食同价不涨价公约》",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "本人/本门店承诺：线上点餐菜品标价与线下堂食菜单保持 100% 一致，不虚标原价、不借满减变相涨价，接受平台与消费者监督。",
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = agreedCovenant,
                            onCheckedChange = { agreedCovenant = it }
                        )
                        Text(
                            text = "我已阅读并电子签署《100%堂食同价不涨价公约》",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    submitMessage = when {
                        storeName.isBlank() || contactPhone.isBlank() || licenseNumber.isBlank() ->
                            "请完整填写门店名称、手机号与营业执照信息"
                        !agreedCovenant ->
                            "请先勾选并签署《100%堂食同价不涨价公约》"
                        else ->
                            "已提交入驻申请（$storeName · 0%抽成），公约签署生效，预计1分钟内完成多租户初始化。"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("提交 0% 抽成入驻申请", fontWeight = FontWeight.Bold)
            }
        }

        submitMessage?.let { msg ->
            item {
                Text(
                    text = msg,
                    color = if (msg.startsWith("已提交")) Color(0xFF1B5E20) else Color(0xFFC62828),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InstantWithdrawPanel() {
    var selectedRole by remember { mutableStateOf(WithdrawRole.MERCHANT) }
    var selectedChannel by remember { mutableStateOf(WithdrawChannel.WECHAT_CHANGE) }
    var withdrawAmount by remember { mutableStateOf("") }
    var accountInput by remember { mutableStateOf("") }
    var withdrawStatus by remember { mutableStateOf<String?>(null) }

    val bills = remember {
        mutableStateListOf(
            WithdrawBillRecord("WD-20261116-01", "商家账户", "微信零钱", "¥3,200.00", "¥0.00", "秒级到账成功", "10:15:08"),
            WithdrawBillRecord("WD-20261115-09", "骑手账户", "支付宝", "¥428.50", "¥0.00", "秒级到账成功", "昨天 21:40"),
            WithdrawBillRecord("WD-20261114-03", "商家账户", "银行卡", "¥8,500.00", "¥0.00", "秒级到账成功", "11-14 18:02")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WithdrawRole.values().forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role.label) }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${selectedRole.label}可提现余额（元）",
                        fontSize = 13.sp,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${selectedRole.defaultBalance}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "全渠道 0 手续费 · 7x24小时秒级到账",
                        fontSize = 12.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }

        item {
            Text("选择秒级收款渠道（0 手续费）", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WithdrawChannel.values().forEach { channel ->
                    FilterChip(
                        selected = selectedChannel == channel,
                        onClick = { selectedChannel = channel },
                        label = { Text(channel.label) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = accountInput,
                onValueChange = { accountInput = it },
                label = { Text("${selectedChannel.label}收款账号 / 卡号") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = withdrawAmount,
                onValueChange = { withdrawAmount = it },
                label = { Text("提现金额（手续费 ¥0.00）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = { withdrawAmount = selectedRole.defaultBalance }) {
                        Text("全部提现")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    val amountVal = withdrawAmount.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0 || accountInput.isBlank()) {
                        withdrawStatus = "请输入有效提现金额和${selectedChannel.label}收款账号"
                    } else {
                        val formatted = String.format("%.2f", amountVal)
                        bills.add(
                            0,
                            WithdrawBillRecord(
                                id = "WD-20261116-${bills.size + 10}",
                                roleLabel = selectedRole.label,
                                channel = selectedChannel.label,
                                amount = "¥$formatted",
                                fee = "¥0.00",
                                status = "秒级到账成功",
                                time = "刚刚"
                            )
                        )
                        withdrawStatus = "提现成功：¥$formatted 已秒级打入${selectedChannel.label}（手续费 ¥0.00）"
                        withdrawAmount = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("确认 0 手续费秒级提现", fontWeight = FontWeight.Bold)
            }
        }

        withdrawStatus?.let { status ->
            item {
                Text(
                    text = status,
                    color = if (status.startsWith("提现成功")) Color(0xFF1B5E20) else Color(0xFFC62828),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Text("0 手续费秒级提现账单", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(bills, key = { it.id }) { bill ->
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${bill.roleLabel} · ${bill.channel}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${bill.id} · ${bill.time}", fontSize = 12.sp, color = Color.Gray)
                        Text("手续费：${bill.fee} · ${bill.status}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                    }
                    Text(bill.amount, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
