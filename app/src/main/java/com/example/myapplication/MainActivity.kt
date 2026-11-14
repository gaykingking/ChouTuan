package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.sdk.VoiceBroadcastManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.di.ChouTuanAppContainer
import com.example.myapplication.domain.model.TransparentBillBreakdown
import com.example.myapplication.ui.components.TransparentBillCard
import com.example.myapplication.ui.screens.GroupBuyVoucherScreen
import com.example.myapplication.ui.screens.GroupDealCardModel
import com.example.myapplication.ui.screens.GroupVoucherCardModel
import com.example.myapplication.ui.screens.HomeTenantCardModel
import com.example.myapplication.ui.screens.MerchantOrderCardModel
import com.example.myapplication.ui.screens.MerchantProductCardModel
import com.example.myapplication.ui.screens.MerchantTenantOptionModel
import com.example.myapplication.ui.screens.MerchantWorkspaceScreen
import com.example.myapplication.ui.screens.OrderRadarCardModel
import com.example.myapplication.ui.screens.OrderRadarTrackScreen
import com.example.myapplication.ui.screens.RiderDeliveryScreen
import com.example.myapplication.ui.screens.RiderTaskCardModel
import com.example.myapplication.ui.screens.SdkConsoleScreen
import com.example.myapplication.ui.screens.UserChannelHomeScreen
import com.example.myapplication.ui.state.ChouTuanIntent
import com.example.myapplication.ui.state.ChouTuanUiState
import com.example.myapplication.ui.viewmodel.ChouTuanViewModel

/**
 * 丑团 ChouTuan v3.0 主入口
 * 基于 SOLID / 依赖倒置原则接入 ChouTuanAppContainer 与 6 大独立 Screen 模块
 */
class MainActivity : ComponentActivity() {
    private val appContainer by lazy { ChouTuanAppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = remember { appContainer.createViewModel() }
                    ChouTuanRootScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChouTuanRootScreen(viewModel: ChouTuanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeModuleIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val voiceManager = remember(context) { VoiceBroadcastManager(context) }
        val voiceBroadcastManager: VoiceBroadcastManager? = voiceManager
    
    val moduleTitles = listOf("点单频道", "团购核销", "订单雷达", "商家工作台", "骑手配送", "SDK联调")

    LaunchedEffect(uiState.toastMessage) {
        val message = uiState.toastMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(ChouTuanIntent.DismissToast)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("丑团 ChouTuan v3.0", fontWeight = FontWeight.Bold)
                        Text(
                            text = "0抽佣 · 全透明账单 · SOLID 架构闭环",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                moduleTitles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = activeModuleIndex == index,
                        onClick = { activeModuleIndex = index },
                        icon = { Text("${index + 1}") },
                        label = { Text(title, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 顶部快捷账单透明化预览卡片（存在购物车商品时展示）
            if (uiState.cart.isNotEmpty()) {
                val sampleBreakdown = TransparentBillBreakdown(
                    storeGoodsTotal = uiState.cart.values.sum() * 18.0,
                    riderDeliveryFee = 3.5,
                    platformTechFee = 0.0,
                    merchantNetIncome = uiState.cart.values.sum() * 18.0,
                    riderNetIncome = 3.5,
                    userPayTotal = uiState.cart.values.sum() * 18.0 + 3.5,
                    savedVsOtherPlatform = uiState.cart.values.sum() * 4.5
                )
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    TransparentBillCard(breakdown = sampleBreakdown)
                }
            }

            // 6 大独立 Screen 模块路由分发
            Box(modifier = Modifier.weight(1f)) {
                when (activeModuleIndex) {
                    0 -> UserChannelHomeScreen(
                        tenants = uiState.toHomeTenantCards(),
                        address = uiState.userAddress,
                        searchQuery = uiState.searchQuery,
                        selectedChannel = uiState.selectedChannel,
                        onSearchChange = { viewModel.onIntent(ChouTuanIntent.UpdateSearchQuery(it)) },
                        onChannelSelect = { viewModel.onIntent(ChouTuanIntent.SelectChannel(it)) },
                        onShopClick = { viewModel.onIntent(ChouTuanIntent.SelectTenant(it)) }
                    )

                    1 -> GroupBuyVoucherScreen(
                        deals = uiState.toGroupDealCards(),
                        vouchers = uiState.toGroupVoucherCards(),
                        onBuyDeal = { deal -> viewModel.onIntent(ChouTuanIntent.BuyGroupDeal(deal.id)); voiceManager.broadcastMerchantOrder("下单支付成功，套餐编号 ${deal.id}", 0.0) }
                    )

                    2 -> OrderRadarTrackScreen(
                        orders = uiState.toOrderRadarCards(),
                        onAdvanceStatus = { orderId ->
                            viewModel.onIntent(ChouTuanIntent.AdvanceOrderStatus(orderId))
                        }
                    )

                    3 -> MerchantWorkspaceScreen(
                        tenants = uiState.toMerchantTenantOptions(),
                        activeTenantId = uiState.activeMerchantTenantId,
                        orders = uiState.toMerchantOrderCards(),
                        products = uiState.toMerchantProductCards(),
                        onSelectTenant = { viewModel.onIntent(ChouTuanIntent.SelectMerchantTenant(it)) },
                        onRedeemVoucher = { viewModel.onIntent(ChouTuanIntent.RedeemVoucher(it)); voiceBroadcastManager?.broadcastMerchantOrder(); dao.appendLog("[TTS] 商家核销团购券($it)触发真机语音播报") },
                        onOpenAddProduct = { viewModel.onIntent(ChouTuanIntent.ToggleAddProductDialog(true)) },
                        onToggleProductAvailability = {
                            viewModel.onIntent(ChouTuanIntent.ToggleProductAvailability(it))
                        },
                        onAcceptOrder = { viewModel.onIntent(ChouTuanIntent.AcceptOrder(it)) ; voiceBroadcastManager?.broadcastMerchantOrder(); dao.appendLog("[TTS] 商家点击接单($it)触发真机语音播报" }) }                    )

                    4 -> RiderDeliveryScreen(
                        tasks = uiState.toRiderTaskCards(),
                        onPickupOrder = { viewModel.onIntent(ChouTuanIntent.PickupOrder(it)) },
                        onCompleteDelivery = { viewModel.onIntent(ChouTuanIntent.CompleteDelivery(it)); voiceBroadcastManager?.broadcastRiderTask(); dao.appendLog("[TTS] 骑手确认送达($it)触发真机语音播报") }
                    )

                    else -> SdkConsoleScreen(
                        address = uiState.userAddress,
                        baseUrl = uiState.baseUrl,
                        amapKey = uiState.amapKey,
                        sdkLogs = uiState.sdkLogs,
                        onAddressChange = { viewModel.onIntent(ChouTuanIntent.UpdateAddress(it)) },
                        onBaseUrlChange = { viewModel.onIntent(ChouTuanIntent.UpdateBaseUrl(it)) },
                        onAmapKeyChange = { viewModel.onIntent(ChouTuanIntent.UpdateAmapKey(it)) },
                        onSimulatePing = { viewModel.onIntent(ChouTuanIntent.SimulateSdkPing) }
                    )
                }
            }
        }
    }
}

private fun ChouTuanUiState.toHomeTenantCards(): List<HomeTenantCardModel> =
    tenants.map {
        HomeTenantCardModel(
            tenantId = it.id,
            name = it.name,
            emoji = it.emoji,
            channelCategory = it.channelCategory,
            rating = it.rating,
            monthlySales = it.monthlySales,
            deliveryTimeMinutes = it.deliveryTimeMinutes,
            distanceKm = it.distanceKm,
            riderFee = it.riderFee,
            otherCommissionRate = it.otherCommissionRate,
            avgSavedPerDish = it.avgSavedPerDish
        )
    }

private fun ChouTuanUiState.toGroupDealCards(): List<GroupDealCardModel> =
    groupDeals.map {
        GroupDealCardModel(
            id = it.id,
            shopName = it.shopName,
            title = it.title,
            storeOriginalValue = it.storeOriginalValue,
            choutuanPrice = it.choutuanPrice,
            otherPlatformPrice = it.otherPlatformPrice,
            emoji = it.emoji
        )
    }

private fun ChouTuanUiState.toGroupVoucherCards(): List<GroupVoucherCardModel> =
    vouchers.map {
        GroupVoucherCardModel(
            code = it.code,
            dealTitle = it.dealTitle,
            shopName = it.shopName,
            price = it.price,
            redeemed = it.redeemed
        )
    }

private fun ChouTuanUiState.toOrderRadarCards(): List<OrderRadarCardModel> =
    orders.map {
        OrderRadarCardModel(
            orderId = it.orderId,
            tenantName = it.tenantName,
            statusLabel = it.status.label,
            progress = it.status.progress,
            shopLatLabel = it.shopLatLabel,
            riderPositionLabel = it.riderPositionLabel,
            userLatLabel = it.userLatLabel,
            paymentChannel = it.paymentChannel.label,
            userPayTotal = it.bill.userPayTotal,
            merchantNetIncome = it.bill.merchantNetIncome,
            riderNetIncome = it.bill.riderNetIncome,
            platformTechFee = it.bill.platformTechFee,
            isCompleted = it.status.progress >= 1f
        )
    }

private fun ChouTuanUiState.toMerchantTenantOptions(): List<MerchantTenantOptionModel> =
    tenants.map {
        MerchantTenantOptionModel(
            tenantId = it.id,
            name = it.name,
            emoji = it.emoji,
            tenantTypeLabel = it.channelCategory,
            otherCommissionRate = it.otherCommissionRate
        )
    }

private fun ChouTuanUiState.toMerchantOrderCards(): List<MerchantOrderCardModel> =
    orders.filter { it.tenantId == activeMerchantTenantId }.map {
        MerchantOrderCardModel(
            orderId = it.orderId,
            merchantNetIncome = it.bill.merchantNetIncome,
            statusLabel = it.status.label,
            isPendingAccept = it.status.progress < 0.3f
        )
    }

private fun ChouTuanUiState.toMerchantProductCards(): List<MerchantProductCardModel> =
    products.filter { it.tenantId == activeMerchantTenantId }.map {
        MerchantProductCardModel(
            productId = it.id,
            name = it.name,
            emoji = it.emoji,
            storePrice = it.storePrice,
            otherPlatformPrice = it.otherPlatformPrice,
            isAvailable = it.isAvailable
        )
    }

private fun ChouTuanUiState.toRiderTaskCards(): List<RiderTaskCardModel> =
    orders.map {
        RiderTaskCardModel(
            orderId = it.orderId,
            tenantName = it.tenantName,
            tenantEmoji = "🛵",
            tenantTypeLabel = "即时配送",
            deliveryAddress = it.deliveryAddress,
            riderNetIncome = it.bill.riderNetIncome,
            statusLabel = it.status.label,
            isPendingPickup = it.status.progress in 0.2f..0.45f,
            isDelivering = it.status.progress in 0.46f..0.95f,
            isCompleted = it.status.progress >= 1f
        )
    }
