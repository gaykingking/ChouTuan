package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.di.ChouTuanAppContainer
import com.example.myapplication.domain.model.TransparentBillBreakdown
import com.example.myapplication.sdk.VoiceBroadcastManager
import com.example.myapplication.ui.components.TransparentBillCard
import com.example.myapplication.ui.screens.GroupBuyVoucherScreen
import com.example.myapplication.ui.screens.GroupDealCardModel
import com.example.myapplication.ui.screens.GroupVoucherCardModel
import com.example.myapplication.ui.screens.HomeTenantCardModel
import com.example.myapplication.ui.screens.MerchantOrderCardModel
import com.example.myapplication.ui.screens.MerchantProductCardModel
import com.example.myapplication.ui.screens.MerchantTenantOptionModel
import com.example.myapplication.ui.screens.MerchantWorkspaceScreen
import com.example.myapplication.ui.screens.OnboardingAndWithdrawScreen
import com.example.myapplication.ui.screens.OrderRadarCardModel
import com.example.myapplication.ui.screens.OrderRadarTrackScreen
import com.example.myapplication.ui.screens.RevenueAnalyticsScreen
import com.example.myapplication.ui.screens.RiderDeliveryScreen
import com.example.myapplication.ui.screens.RiderTaskCardModel
import com.example.myapplication.ui.screens.SdkConsoleScreen
import com.example.myapplication.ui.screens.UserChannelHomeScreen
import com.example.myapplication.ui.state.ChouTuanIntent
import com.example.myapplication.ui.viewmodel.ChouTuanViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { ChouTuanAppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = appContainer.createViewModel()
        setContent {
val themePrefs = remember { getSharedPreferences("choutuan_theme_prefs", Context.MODE_PRIVATE) }
var isDarkTheme by remember { mutableStateOf(themePrefs.getBoolean("dark_theme", false)) }
           MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChouTuanMainShell(viewModel = viewModel, isDarkTheme = isDarkTheme, onToggleDarkTheme = { val next = !isDarkTheme; isDarkTheme = next; themePrefs.edit().putBoolean("dark_theme", next).apply() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChouTuanMainShell(
    viewModel: ChouTuanViewModel,
isDarkTheme: Boolean,
onToggleDarkTheme: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val voicePrefs = remember(context) {
        context.getSharedPreferences("choutuan_voice_prefs", Context.MODE_PRIVATE)
    }
    val voiceManager = remember(context) {
        VoiceBroadcastManager(context).apply {
            setMuted(voicePrefs.getBoolean("voice_muted", false))
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(voiceManager.isMuted) }
    var selectedTenantForBill by remember { mutableStateOf<HomeTenantCardModel?>(null) }
    var activeBillPreview by remember { mutableStateOf<TransparentBillBreakdown?>(null) }

    val homeTenants = remember {
        mutableStateListOf(
            HomeTenantCardModel(
                tenantId = "tenant_food_1",
                name = "大兴观音寺黄焖鸡米饭",
                emoji = "🍲",
                channelCategory = "美食",
                rating = 4.9,
                monthlySales = 1280,
                deliveryTimeMinutes = 26,
                distanceKm = 1.2,
                riderFee = 4.0,
                otherCommissionRate = 23,
                avgSavedPerDish = 8.0
            ),
            HomeTenantCardModel(
                tenantId = "tenant_mart_1",
                name = "首座御园 24h 便利超市",
                emoji = "🛒",
                channelCategory = "超市便利",
                rating = 4.8,
                monthlySales = 940,
                deliveryTimeMinutes = 19,
                distanceKm = 0.6,
                riderFee = 3.5,
                otherCommissionRate = 20,
                avgSavedPerDish = 5.5
            ),
            HomeTenantCardModel(
                tenantId = "tenant_tea_1",
                name = "观音寺手作鲜果茶",
                emoji = "🧋",
                channelCategory = "奶茶甜品",
                rating = 4.9,
                monthlySales = 2150,
                deliveryTimeMinutes = 22,
                distanceKm = 0.9,
                riderFee = 3.0,
                otherCommissionRate = 22,
                avgSavedPerDish = 4.0
            )
        )
    }

    val groupDeals = remember {
        mutableStateListOf(
            GroupDealCardModel(
                id = "deal_1",
                shopName = "大兴观音寺黄焖鸡米饭",
                title = "招牌黄焖鸡双人透明套餐",
                storeOriginalValue = 68.0,
                choutuanPrice = 49.9,
                otherPlatformPrice = 62.0,
                emoji = "🍲"
            ),
            GroupDealCardModel(
                id = "deal_2",
                shopName = "观音寺手作鲜果茶",
                title = "鲜果茶双杯畅饮券",
                storeOriginalValue = 36.0,
                choutuanPrice = 24.9,
                otherPlatformPrice = 32.0,
                emoji = "🧋"
            )
        )
    }

    val groupVouchers = remember {
        mutableStateListOf(
            GroupVoucherCardModel(
                code = "CT-8891-2026",
                dealTitle = "招牌黄焖鸡双人透明套餐",
                shopName = "大兴观音寺黄焖鸡米饭",
                price = 49.9,
                redeemed = false
            )
        )
    }

    val radarOrders = remember {
        mutableStateListOf(
            OrderRadarCardModel(
                orderId = "ORD-20261121-01",
                tenantName = "大兴观音寺黄焖鸡米饭",
                statusLabel = "骑手配送中",
                progress = 0.65f,
                shopLatLabel = "门店: 观音寺北里",
                riderPositionLabel = "骑手距您 420m",
                userLatLabel = "送达: 首座御园 3号楼",
                paymentChannel = "微信直连分账",
                userPayTotal = 42.0,
                merchantNetIncome = 37.6,
                riderNetIncome = 4.0,
                platformTechFee = 0.4,
                isCompleted = false
            )
        )
    }

    val merchantTenants = remember {
        mutableStateListOf(
            MerchantTenantOptionModel(
                tenantId = "tenant_food_1",
                name = "大兴观音寺黄焖鸡米饭",
                emoji = "🍲",
                tenantTypeLabel = "餐饮外卖",
                otherCommissionRate = 23
            ),
            MerchantTenantOptionModel(
                tenantId = "tenant_mart_1",
                name = "首座御园 24h 便利超市",
                emoji = "🛒",
                tenantTypeLabel = "即时零售",
                otherCommissionRate = 20
            )
        )
    }

    val merchantOrders = remember {
        mutableStateListOf(
            MerchantOrderCardModel(
                orderId = "ORD-20261121-01",
                merchantNetIncome = 37.6,
                statusLabel = "待接单",
                isPendingAccept = true
            ),
            MerchantOrderCardModel(
                orderId = "ORD-20261121-02",
                merchantNetIncome = 54.2,
                statusLabel = "已出餐",
                isPendingAccept = false
            )
        )
    }

    val merchantProducts = remember {
        mutableStateListOf(
            MerchantProductCardModel(
                productId = "prod_1",
                name = "招牌黄焖鸡大份(微辣)",
                emoji = "🍲",
                storePrice = 28.0,
                otherPlatformPrice = 36.0,
                isAvailable = true
            ),
            MerchantProductCardModel(
                productId = "prod_2",
                name = "秘制香辣排骨煲",
                emoji = "🍖",
                storePrice = 38.0,
                otherPlatformPrice = 48.0,
                isAvailable = true
            )
        )
    }

    val riderTasks = remember {
        mutableStateListOf(
            RiderTaskCardModel(
                orderId = "ORD-20261121-01",
                tenantName = "大兴观音寺黄焖鸡米饭",
                tenantEmoji = "🥘",
                tenantTypeLabel = "餐饮外卖租户",
                deliveryAddress = uiState.deliveryAddress,
                riderNetIncome = 6.5,
                statusLabel = "待抢单",
                isPendingPickup = true,
                isDelivering = false,
                isCompleted = false
            )
        )
    }

    val filteredTenants = remember(homeTenants.toList(), uiState.selectedChannel, uiState.searchQuery) {
        homeTenants.filter { tenant ->
            val matchChannel = uiState.selectedChannel == "全部" || tenant.channelCategory == uiState.selectedChannel
            val matchQuery = uiState.searchQuery.isBlank() ||
                tenant.name.contains(uiState.searchQuery, ignoreCase = true) ||
                tenant.channelCategory.contains(uiState.searchQuery, ignoreCase = true)
            matchChannel && matchQuery
        }
    }

    val roleOptions = listOf(
        "USER" to "用户端",
        "MERCHANT" to "商家端(多租户)",
        "RIDER" to "骑手端"
    )

    val userTabs = listOf("点单频道", "团购核销", "订单雷达", "入驻/提现", "SDK联调")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("臭团 ChouTuan · 0佣金透明外卖") },
                    actions = {
                        TextButton(
                            onClick = {
                                voiceManager.toggleMute()
                                isMuted = voiceManager.isMuted
                                voicePrefs.edit().putBoolean("voice_muted", isMuted).apply()
                            }
                        ) {
                            Text(if (isMuted) "🔇 已静音" else "🔊 语音开")
                        }
TextButton(onClick = onToggleDarkTheme) {
    Text(if (isDarkTheme) "☀️" else "🌙")
}
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roleOptions.forEach { (roleKey, roleLabel) ->
                        FilterChip(
                            selected = uiState.activeRoleName == roleKey,
                            onClick = { viewModel.onIntent(ChouTuanIntent.SwitchRole(roleKey)) },
                            label = { Text(roleLabel) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.activeRoleName == "USER") {
                NavigationBar {
                    userTabs.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = { Text(label.take(1)) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeRoleName) {
                "MERCHANT" -> {
                    var showMerchantAnalytics by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = !showMerchantAnalytics, onClick = { showMerchantAnalytics = false }, label = { Text("工作台") })
                                    FilterChip(selected = showMerchantAnalytics, onClick = { showMerchantAnalytics = true }, label = { Text("7日收益报表") })
                        }
                          if (showMerchantAnalytics) { RevenueAnalyticsScreen() } else {
                   MerchantWorkspaceScreen(
                        tenants = merchantTenants,
                        activeTenantId = uiState.activeTenantId,
                        orders = merchantOrders,
                        products = merchantProducts,
                        onSelectTenant = { tenantId ->
                            viewModel.onIntent(ChouTuanIntent.SwitchMerchantTenant(tenantId))
                        },
                       onRedeemVoucher = { code -> viewModel.onIntent(ChouTuanIntent.RedeemVoucher(code)) },
                        onOpenAddProduct = { },
                        onToggleProductAvailability = { productId ->
                            val idx = merchantProducts.indexOfFirst { it.productId == productId }
                            if (idx >= 0) {
                                merchantProducts[idx] = merchantProducts[idx].copy(isAvailable = !merchantProducts[idx].isAvailable)
                            }
                        },
                        onAcceptOrder = { orderId ->
                            val idx = merchantOrders.indexOfFirst { it.orderId == orderId }
                            if (idx >= 0) {
                                val order = merchantOrders[idx]
                                merchantOrders[idx] = order.copy(statusLabel = "已接单备餐中", isPendingAccept = false)
                                voiceManager.broadcastMerchantOrder(order.merchantNetIncome.toString())
                            }
                        }
                    )
                          }
                    }
                }

                "RIDER" -> {
                    RiderDeliveryScreen(
                        tasks = riderTasks,
                        onPickupOrder = { orderId ->
                            val idx = riderTasks.indexOfFirst { it.orderId == orderId }
                            if (idx >= 0) {
                                riderTasks[idx] = riderTasks[idx].copy(statusLabel = "已抢单取餐中", isPendingPickup = false, isDelivering = true)
                            }
                        },
                       onCompleteDelivery = { orderId ->
                            val idx = riderTasks.indexOfFirst { it.orderId ==orderId }
                            if (idx >= 0) {
                                riderTasks[idx] = riderTasks[idx].copy(statusLabel = "已送达", isDelivering = false, isCompleted = true)
                            }
                        }
                    )
                }

                else -> {
                    when (selectedTabIndex) {
                        0 -> UserChannelHomeScreen(
                            selectedChannel = uiState.selectedChannel,
                            searchQuery = uiState.searchQuery,
                            address= uiState.deliveryAddress,
                            tenants = filteredTenants,
                            onChannelSelect = { channel ->
                                viewModel.onIntent(ChouTuanIntent.SelectChannel(channel))
                            },
                            onSearchChange = { query ->
                                viewModel.onIntent(ChouTuanIntent.UpdateSearchQuery(query))
                            },
                           onShopClick = { tenantId ->
                               homeTenants.find { it.tenantId == tenantId }?.let { tenant ->
                                val bill = viewModel.previewTransparentBill(
                                    items = listOf(Triple(38.0, 38.0, 1)),
                                    otherPrices = listOf(Pair(46.0, 1)),
                                    riderFee = tenant.riderFee,
                                    otherCommissionRate = tenant.otherCommissionRate
                                )
                                selectedTenantForBill = tenant
                                activeBillPreview = bill
                               }
                           }
                        )

                        1 -> GroupBuyVoucherScreen(
                            deals = groupDeals,
                            vouchers = groupVouchers,
                            onBuyDeal = { deal ->
                                val newCode = "CT-${(1000..9999).random()}-2026"
                                groupVouchers.add(
                                    0,
                                    GroupVoucherCardModel(
                                        code = newCode,
                                        dealTitle = deal.title,
                                        shopName = deal.shopName,
                                        price = deal.choutuanPrice,
                                        redeemed = false
                                    )
                                )
                                Toast.makeText(context, "已购券: $newCode", Toast.LENGTH_SHORT).show()
                         }
                        )

                        2 -> OrderRadarTrackScreen(
                            orders = radarOrders,
                            onAdvanceStatus = { orderId ->
                                val idx = radarOrders.indexOfFirst { it.orderId == orderId }
                                if (idx >= 0 && !radarOrders[idx].isCompleted) {
                                    val order = radarOrders[idx]
                                    val nextProgress = (order.progress + 0.35f).coerceAtMost(1.0f)
                                    val done = nextProgress >= 1.0f
                                    radarOrders[idx] = order.copy(
                                        progress = nextProgress,
                                        statusLabel = if (done) "已送达" else "骑手即将到达",
                                        riderPositionLabel = if (done) "已送达指定位置" else "距您 100m",
                                        isCompleted = done
                                    )
                                }
                            }
                        )

                        3 -> OnboardingAndWithdrawScreen()

                        else -> SdkConsoleScreen(
                            address = uiState.deliveryAddress,
                            baseUrl = "api.choutuan.local/v1",
                            amapKey = "AMAP_KEY_CHOUTUAN",
                            sdkLogs = uiState.sdkLogs,
                            onAddressChange = {},
                            onBaseUrlChange = {},
                            onAmapKeyChange = {},
                            onSimulatePing = {}
                        )
                    }
                }
            }
        }
    }

    val billToShow = activeBillPreview
    val tenantToShow = selectedTenantForBill
    if (billToShow != null && tenantToShow != null) {
        AlertDialog(
            onDismissRequest = {
                activeBillPreview = null
                selectedTenantForBill = null
            },
            title = { Text("${tenantToShow.emoji} ${tenantToShow.name} · 透明账单") },
            text = {
                TransparentBillCard(breakdown = billToShow)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        radarOrders.add(
                            0,
                            OrderRadarCardModel(
                                orderId = "ORD-${(1000..9999).random()}",
                                tenantName = tenantToShow.name,
                                statusLabel = "已下单待接单",
                                progress = 0.15f,
                                shopLatLabel = "门店: ${tenantToShow.name}",
                                riderPositionLabel = "等待骑手接单",
                                userLatLabel = "送达: ${uiState.deliveryAddress}",
                                paymentChannel = "微信直连分账",
                                userPayTotal = 38.0 + tenantToShow.riderFee,
                                merchantNetIncome = 38.0,
                                riderNetIncome = tenantToShow.riderFee,
                                platformTechFee = 0.4,
                                isCompleted = false
                            )
                        )
                        activeBillPreview = null
                        selectedTenantForBill = null
                        selectedTabIndex = 2
                        Toast.makeText(context, "下单成功，已跳转订单雷达", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确认透明下单")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        activeBillPreview = null
                        selectedTenantForBill = null
                    }
                ) {
                    Text("关闭")
                }
            }
        )
    }
}
