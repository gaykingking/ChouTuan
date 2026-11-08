package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChouTuanApp()
            }
        }
    }
}

val BrandOrange = Color(0xFFFF5722)
val BrandYellow = Color(0xFFFFC107)
val FairGreen = Color(0xFF16A34A)
val FairGreenBg = Color(0xFFDCFCE7)
val DarkSlate = Color(0xFF0F172A)
val SoftGrayBg = Color(0xFFF8FAFC)

enum class AppRole(val label: String, val badge: String) {
    USER("用户端", "全频道0溢价"),
    MERCHANT("商家端(多租户)", "0%抽成工作台"),
    RIDER("骑手端", "运费100%到账")
}

enum class TenantType(val label: String, val emoji: String) {
    FOOD("餐饮外卖租户", "🥘"),
    FLASH("丑团闪购租户", "⚡"),
    SUPERMARKET("生鲜超市租户", "🥬"),
    GROUP_BUY("到店团购租户", "🎫"),
    PHARMACY("平价买药租户", "💊")
}

enum class UserTab(val title: String, val icon: String) {
    HOME("外卖/全频道", "🍱"),
    FLASH_SUPER("闪购超市", "⚡"),
    GROUP_BUY("到店团购", "🎫"),
    ORDERS("透明订单", "🧾"),
    ARCH("我的/架构", "🛡️")
}

enum class OrderStatus(val label: String, val desc: String) {
    PENDING("待租户接单", "订单直达商家租户，平台0抽成不压单"),
    PREPARING("备餐/拣货中", "门店同价现做现拣，拒绝料理包"),
    DELIVERING("骑手配送中", "骑手全额拿运费，温和算法不极限催命"),
    COMPLETED("已送达", "账单已按透明合约100%自动分账")
}

data class ProductItem(
    val id: String,
    val category: String,
    val name: String,
    val storePrice: Double,
    val otherPlatformPrice: Double,
    val packFee: Double,
    val emoji: String,
    val available: Boolean = true
)

data class MerchantTenant(
    val tenantId: String,
    val name: String,
    val tenantType: TenantType,
    val channel: String,
    val emoji: String,
    val rating: Double,
    val monthlySales: Int,
    val deliveryMinutes: Int,
    val distanceKm: Double,
    val minOrder: Double,
    val riderFee: Double,
    val techFee: Double = 0.2,
    val otherCutRate: Int = 23,
    val notice: String,
    val products: List<ProductItem>
)

data class CartEntry(val item: ProductItem, val count: Int)

data class TransparentOrder(
    val orderId: String,
    val tenantId: String,
    val tenantName: String,
    val tenantType: TenantType,
    val entries: List<CartEntry>,
    val goodsTotal: Double,
    val packTotal: Double,
    val riderFee: Double,
    val techFee: Double,
    val savedAmount: Double,
    val address: String,
    val status: OrderStatus
) {
    val userPayTotal: Double get() = goodsTotal + packTotal + riderFee + techFee
    val merchantNet: Double get() = goodsTotal + packTotal
}

// ======================= 预留 Room / Retrofit / 地图 / 支付 SDK 对接结构 =======================
interface ChouTuanRoomDaoContract {
    suspend fun upsertTenants(tenants: List<MerchantTenant>)
    suspend fun insertOrder(order: TransparentOrder)
    suspend fun queryOrders(): List<TransparentOrder>
}

interface ChouTuanRetrofitApiContract {
    suspend fun fetchTenants(channel: String?, lat: Double, lng: Double): List<MerchantTenant>
    suspend fun submitTransparentOrder(tenantId: String, order: TransparentOrder): Boolean
}

interface MapLocationSdkContract {
    fun initMapSdk(apiKey: String)
    fun currentAddress(): String = "阳光花园 3栋 1202室"
}

interface SplitPaymentSdkContract {
    suspend fun payAndSplit(orderId: String, merchantNet: Double, riderFee: Double, techFee: Double): Boolean
}

object LocalMockData {
    val tenants = listOf(
        MerchantTenant(
            "t_food_1", "老街坊现炒木桶饭（总店）", TenantType.FOOD, "快餐便当", "🥘", 4.9, 3280, 28, 1.2, 15.0, 3.5, 0.2, 23,
            "退出高抽成凑满减套路，全场100%堂食同价，猛火现炒！",
            listOf(
                ProductItem("p1", "招牌现炒", "农家辣椒炒肉木桶饭", 18.0, 25.8, 1.0, "🥩"),
                ProductItem("p2", "招牌现炒", "茶油仔姜炒土鸡饭", 22.0, 29.9, 1.0, "🍗"),
                ProductItem("p3", "加菜卤味", "慢卤入味大鸡腿", 6.0, 9.9, 0.0, "🍗")
            )
        ),
        MerchantTenant(
            "t_flash_1", "丑团闪购·24h社区便利仓", TenantType.FLASH, "丑团闪购", "⚡", 4.9, 6420, 18, 0.7, 15.0, 3.0, 0.2, 20,
            "数码应急、冰镇酒水、零食百货24小时闪送，超市同价0溢价！",
            listOf(
                ProductItem("pf1", "冰镇酒水", "精酿小麦白啤听装(6听)", 29.9, 42.0, 0.5, "🍺"),
                ProductItem("pf2", "数码应急", "Type-C 65W快充线", 12.9, 25.0, 0.0, "🔌")
            )
        ),
        MerchantTenant(
            "t_super_1", "兴农直供·平价生鲜超市", TenantType.SUPERMARKET, "生鲜超市", "🥬", 4.9, 8300, 25, 1.1, 20.0, 3.0, 0.2, 20,
            "菜场直连，蔬菜水果鲜肉与早市完全同价，0平台加价！",
            listOf(
                ProductItem("ps1", "每日鲜蔬", "高山有机空心菜(500g)", 3.5, 5.9, 0.0, "🥬"),
                ProductItem("ps2", "肉禽蛋奶", "谷饲鲜土鸡蛋(15枚)", 12.8, 17.9, 0.5, "🥚"),
                ProductItem("ps3", "时令鲜果", "海南树熟贵妃芒(1kg)", 14.9, 21.8, 0.5, "🥭")
            )
        ),
        MerchantTenant(
            "t_pharm_1", "康民大药房·24h平价买药", TenantType.PHARMACY, "平价买药", "💊", 5.0, 3100, 16, 0.9, 10.0, 3.0, 0.2, 24,
            "实体药店医保同价，拒绝夜间急用药品高溢价！",
            listOf(
                ProductItem("pp1", "家庭常备", "医用无菌创可贴(100片)", 7.9, 14.9, 0.0, "🩹"),
                ProductItem("pp2", "退热护理", "物理降温冰宝贴(6贴)", 9.9, 18.8, 0.0, "🧊")
            )
        )
    )
}

@Composable
fun ChouTuanApp() {
    var role by remember { mutableStateOf(AppRole.USER) }
    var tab by remember { mutableStateOf(UserTab.HOME) }
    var tenants by remember { mutableStateOf(LocalMockData.tenants) }
    var activeTenantId by remember { mutableStateOf(tenants.first().tenantId) }
    var selectedShopId by remember { mutableStateOf<String?>(null) }
    var selectedChannel by remember { mutableStateOf("全部") }
    var carts by remember {
        mutableStateOf(mapOf("t_food_1" to listOf(CartEntry(LocalMockData.tenants[0].products[0], 1))))
    }
    var orders by remember {
        mutableStateOf(
            listOf(
                TransparentOrder(
                    "CT2026110701", "t_food_1", "老街坊现炒木桶饭（总店）", TenantType.FOOD,
                    listOf(CartEntry(LocalMockData.tenants[0].products[0], 1)),
                    18.0, 1.0, 3.5, 0.2, 7.8, "阳光花园 3栋 1202室", OrderStatus.DELIVERING
                )
            )
        )
    }
    var checkoutShop by remember { mutableStateOf<MerchantTenant?>(null) }

    val selectedShop = tenants.find { it.tenantId == selectedShopId }
    BackHandler(enabled = selectedShop != null) { selectedShopId = null }

    Scaffold(
        topBar = {
            Surface(color = DarkSlate) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.background(BrandOrange, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("丑团", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("com.choutuan.app · 0%抽成 · 拒绝两头吃 · 多租户平台", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppRole.entries.forEach { r ->
                            Surface(
                                color = if (role == r) BrandOrange else Color(0xFF334155),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable { role = r; selectedShopId = null }
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(r.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(r.badge, color = BrandYellow, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (role == AppRole.USER && selectedShop == null) {
                NavigationBar(containerColor = Color.White) {
                    UserTab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = { Text(t.icon) },
                            label = { Text(t.title, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(SoftGrayBg).padding(padding)) {
            when (role) {
                AppRole.USER -> {
                    if (selectedShop != null) {
                        ShopDetailView(
                            shop = selectedShop,
                            entries = carts[selectedShop.tenantId].orEmpty(),
                            onBack = { selectedShopId = null },
                            onAdd = { item ->
                                val list = carts[selectedShop.tenantId].orEmpty().toMutableList()
                                val i = list.indexOfFirst { it.item.id == item.id }
                                if (i >= 0) list[i] = list[i].copy(count = list[i].count + 1) else list.add(CartEntry(item, 1))
                                carts = carts + (selectedShop.tenantId to list)
                            },
                            onCheckout = { checkoutShop = selectedShop }
                        )
                    } else {
                        when (tab) {
                            UserTab.HOME, UserTab.FLASH_SUPER -> {
                                val channels = listOf("全部", "快餐便当", "丑团闪购", "生鲜超市", "到店团购", "平价买药")
                                val list = tenants.filter {
                                    if (tab == UserTab.FLASH_SUPER) it.tenantType != TenantType.FOOD
                                    else (selectedChannel == "全部" || it.channel == selectedChannel)
                                }
                                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    item {
                                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("丑团三不公约：商家0%佣金 · 100%门店同价 · 运费100%归骑手", fontWeight = FontWeight.ExtraBold, color = BrandOrange, fontSize = 13.sp)
                                                Text("已内置：餐饮外卖 / 丑团闪购 / 生鲜超市 / 到店团购 / 平价买药", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    item {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(channels) { ch ->
                                                FilterChip(
                                                    selected = selectedChannel == ch,
                                                    onClick = { if (ch == "到店团购") tab = UserTab.GROUP_BUY else selectedChannel = ch },
                                                    label = { Text(ch) }
                                                )
                                            }
                                        }
                                    }
                                    items(list, key = { it.tenantId }) { shop ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().clickable { selectedShopId = shop.tenantId }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("${shop.emoji} ${shop.name}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                                    Text("${shop.channel} · 0%抽成", color = FairGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Text("⭐ ${shop.rating} | 月售 ${shop.monthlySales} | ${shop.deliveryMinutes}分钟 | 骑手直达 ¥${shop.riderFee}", fontSize = 11.sp, color = Color.Gray)
                                                Text("💡 某团抽成${shop.otherCutRate}%被迫涨价 → 丑团恢复门店原价", fontSize = 11.sp, color = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                            UserTab.GROUP_BUY -> {
                                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    item {
                                        Text("🎫 到店团购 · 0抽成真直降（随时退/过期自动退）", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    }
                                    item {
                                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                                Text("🥘 老街坊双人现炒3菜1汤堂食套餐", fontWeight = FontWeight.Bold)
                                                Text("丑团价 ¥39.9  (某团价 ¥52.0 / 门市价 ¥58.0)", color = BrandOrange, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }
                            UserTab.ORDERS -> {
                                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(orders, key = { it.orderId }) { o ->
                                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                                Text("${o.tenantName} (${o.status.label})", fontWeight = FontWeight.Bold)
                                                Text("透明分账回执：实付 ¥${o.userPayTotal} = 商家实收 ¥${o.merchantNet} + 骑手实得 ¥${o.riderFee} + 通道费 ¥${o.techFee} (平台0抽成)", fontSize = 11.sp, color = FairGreen)
                                            }
                                        }
                                    }
                                }
                            }
                            UserTab.ARCH -> {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🔌 已预留对接结构（见源码底部契约接口）", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Text("1. Room 本地数据库层：ChouTuanRoomDaoContract")
                                    Text("2. Retrofit 多租户网络层：ChouTuanRetrofitApiContract")
                                    Text("3. 高德地图定位与轨迹 SDK：MapLocationSdkContract")
                                    Text("4. 微信/支付宝透明分账 SDK：SplitPaymentSdkContract")
                                }
                            }
                        }
                    }
                }
                AppRole.MERCHANT -> {
                    val currentTenant = tenants.find { it.tenantId == activeTenantId } ?: tenants.first()
                    val tOrders = orders.filter { it.tenantId == currentTenant.tenantId }
                    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("🏢 多租户商家切换器（餐饮/闪购/超市/买药）", fontWeight = FontWeight.ExtraBold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tenants, key = { it.tenantId }) { t ->
                                    FilterChip(
                                        selected = t.tenantId == currentTenant.tenantId,
                                        onClick = { activeTenantId = t.tenantId },
                                        label = { Text("${t.emoji} ${t.name}") }
                                    )
                                }
                            }
                        }
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = FairGreenBg)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text("${currentTenant.name} · 0%抽成独立账本", fontWeight = FontWeight.ExtraBold, color = FairGreen)
                                    Text("本租户订单数：${tOrders.size} | 平台佣金：¥0.00 (0%)", fontSize = 12.sp)
                                }
                            }
                        }
                        items(tOrders, key = { it.orderId }) { o ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("订单 ${o.orderId} · 净收 ¥${o.merchantNet}", fontWeight = FontWeight.Bold)
                                        Text(o.status.label, fontSize = 12.sp, color = BrandOrange)
                                    }
                                    if (o.status == OrderStatus.PENDING) {
                                        Button(onClick = {
                                            orders = orders.map { if (it.orderId == o.orderId) it.copy(status = OrderStatus.PREPARING) else it }
                                        }) { Text("0抽成接单") }
                                    }
                                }
                            }
                        }
                    }
                }
                AppRole.RIDER -> {
                    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("🛵 骑手抢单大厅 · 配送费100%归骑手（0克扣）", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                        items(orders, key = { it.orderId }) { o ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("取货：${o.tenantName}", fontWeight = FontWeight.Bold)
                                        Text("配送费 ¥${o.riderFee} 全额到账 · ${o.status.label}", fontSize = 12.sp, color = FairGreen)
                                    }
                                    if (o.status != OrderStatus.COMPLETED) {
                                        Button(onClick = {
                                            val next = if (o.status == OrderStatus.DELIVERING) OrderStatus.COMPLETED else OrderStatus.DELIVERING
                                            orders = orders.map { if (it.orderId == o.orderId) it.copy(status = next) else it }
                                        }) { Text(if (o.status == OrderStatus.DELIVERING) "确认送达" else "抢单取货") }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            checkoutShop?.let { shop ->
                val entries = carts[shop.tenantId].orEmpty()
                val goods = entries.sumOf { it.item.storePrice * it.count }
                val pack = entries.sumOf { it.item.packFee * it.count }
                val saved = entries.sumOf { (it.item.otherPlatformPrice - it.item.storePrice) * it.count }
                AlertDialog(
                    onDismissRequest = { checkoutShop = null },
                    title = { Text("🧾 丑团透明账单结算公示", fontWeight = FontWeight.ExtraBold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🏪 商家租户实收：¥${goods + pack} (100%到账)", color = FairGreen, fontWeight = FontWeight.Bold)
                            Text("🛵 骑手实得运费：¥${shop.riderFee} (0平台克扣)", color = FairGreen, fontWeight = FontWeight.Bold)
                            Text("⚙️ 支付通道成本：¥${shop.techFee} (固定极低成本)")
                            Text("🚫 丑团平台抽成：¥0.00 (0%)", color = BrandOrange, fontWeight = FontWeight.ExtraBold)
                            Text("🔥 本单比某团同款立省 ¥${(saved * 10).roundToInt() / 10.0}", color = Color.Red, fontSize = 12.sp)
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val newOrder = TransparentOrder(
                                "CT202611070${orders.size + 1}", shop.tenantId, shop.name, shop.tenantType,
                                entries, goods, pack, shop.riderFee, shop.techFee, saved, "阳光花园 3栋 1202室", OrderStatus.PENDING
                            )
                            orders = listOf(newOrder) + orders
                            checkoutShop = null
                            selectedShopId = null
                            tab = UserTab.ORDERS
                        }) { Text("确认支付并分账") }
                    }
                )
            }
        }
    }
}

@Composable
fun ShopDetailView(
    shop: MerchantTenant,
    entries: List<CartEntry>,
    onBack: () -> Unit,
    onAdd: (ProductItem) -> Unit,
    onCheckout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Surface(color = DarkSlate) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← 返回列表", color = Color.White) }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("${shop.emoji} ${shop.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("0%平台抽成 · 门店堂食同价", color = FairGreen, fontSize = 11.sp)
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = FairGreenBg)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Text("📢 租户透明承诺：${shop.notice}", color = FairGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(shop.products, key = { it.id }) { p ->
                    val count = entries.find { it.item.id == p.id }?.count ?: 0
                    Card(colors = CardDefaults.cardColors(containerColor = SoftGrayBg)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.emoji, fontSize = 28.sp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(p.name, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("丑团直供价 ¥${p.storePrice}", color = BrandOrange, fontWeight = FontWeight.ExtraBold)
                                        Spacer(Modifier.width(6.dp))
                                        Text("某团 ¥${p.otherPlatformPrice}", color = Color.Gray, fontSize = 11.sp, textDecoration = TextDecoration.LineThrough)
                                    }
                                }
                            }
                            Button(onClick = { onAdd(p) }) {
                                Text(if (count > 0) "已点 $count" else "+ 加一份")
                            }
                        }
                    }
                }
            }
        }
        if (entries.isNotEmpty()) {
            val total = entries.sumOf { it.item.storePrice * it.count }
            Surface(shadowElevation = 8.dp, color = DarkSlate) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("合计：¥${total} + 运费¥${shop.riderFee}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("已由智能合约锁定 0%抽成", color = FairGreen, fontSize = 10.sp)
                    }
                    Button(onClick = onCheckout, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) {
                        Text("去透明结算", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
