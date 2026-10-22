package uz.meduza.anime.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.data.models.PaymentPlanDto

fun planSavingPercent(plan: PaymentPlanDto, plans: List<PaymentPlanDto>): Int {
    if (plan.days <= 0 || plan.price <= 0) return 0
    val base =
        plans
            .filter { it.currency == plan.currency && it.days > 0 && it.price > 0 }
            .minByOrNull { it.days } ?: return 0
    if (plan.days <= base.days) return 0
    return ((1 - (plan.price.toDouble() / plan.days) / (base.price.toDouble() / base.days)) * 100)
        .roundToInt()
        .coerceIn(0, 99)
}

private fun money(value: Long) =
    NumberFormat.getIntegerInstance(Locale.US).format(value).replace(',', ' ')

@Composable
fun PremiumScreen(onBack: () -> Unit, viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var order by rememberSaveable { mutableStateOf<String?>(null) }
    var browserError by remember { mutableStateOf<String?>(null) }
    val available = state.plans.filter { it.days > 0 && it.price > 0 }
    LaunchedEffect(available) {
        if (available.none { it.planType == selected }) selected = available.firstOrNull()?.planType
    }
    LaunchedEffect(order, lifecycle) {
        val id = order ?: return@LaunchedEffect
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                viewModel.checkPaymentStatus(id) { paid -> if (paid) order = null }
                delay(8000)
            }
        }
    }
    Scaffold(
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Surface(color = SurfaceDark) {
                Column(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (order == null && state.paymentSuccessMessage == null)
                        Button(
                            onClick = {
                                val plan = selected ?: return@Button
                                browserError = null
                                viewModel.createPayment(plan) { url, id ->
                                    val uri = Uri.parse(url)
                                    if (uri.scheme != "https" || uri.host.isNullOrBlank())
                                        browserError =
                                            "To‘lov havolasi ochilmadi. Qayta urinib ko‘ring."
                                    else
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            order = id
                                        }
                                            .onFailure {
                                                browserError =
                                                    "Brauzerni ochib bo‘lmadi. Qurilmada brauzer mavjudligini tekshiring."
                                            }
                                }
                            },
                            enabled =
                                selected != null &&
                                    available.any { it.planType == selected } &&
                                    !state.isCreatingPayment,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                        ) {
                            Text(
                                if (state.isCreatingPayment) "Tayyorlanmoqda…"
                                else "Obunani faollashtirish",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    Text(
                        "To‘lov xavfsiz sahifada amalga oshiriladi.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Orqaga")
                    }
                    Text("Obuna", style = MaterialTheme.typography.titleMedium)
                }
            }
            item {
                Column(
                    Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        Icons.Outlined.WorkspacePremium,
                        null,
                        tint = PremiumGold,
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        "Yana ko‘proq\nMeduza.",
                        color = TextWhite,
                        fontSize = 36.sp,
                        lineHeight = 41.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Sevimli hikoyalaringizdan cheklovlarsiz zavqlaning.",
                        color = TextGray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                    listOf(
                            "Premium qismlarga kirish",
                            "Yangi premyeralar va dublyajlar",
                            "Barcha qurilmalarda shaxsiy hisob",
                        )
                        .forEach { benefit ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    null,
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(benefit, color = TextGray, fontSize = 13.sp)
                            }
                        }
                    Spacer(Modifier.height(6.dp))
                    Text("Sizga mos tarif", style = MaterialTheme.typography.titleLarge)
                }
            }
            if (state.isLoadingPlans)
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            else if (available.isEmpty())
                item {
                    EmptyState(
                        "Tariflar yuklanmadi",
                        "Internet aloqasini tekshirib, yana urinib ko‘ring.",
                        actionLabel = "Qayta urinish",
                        onAction = viewModel::loadPaymentPlans,
                    )
                }
            items(available, key = { it.planType }) { plan ->
                val active = plan.planType == selected
                val saving = planSavingPercent(plan, available)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) CardDarkElevated else SurfaceDark,
                    border =
                        androidx.compose.foundation.BorderStroke(
                            if (active) 1.5.dp else 1.dp,
                            if (active) PrimaryCyan else DividerDark,
                        ),
                    modifier =
                        Modifier.padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .selectable(
                                selected = active,
                                role = Role.RadioButton,
                                onClick = { selected = plan.planType },
                            ),
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                plan.title,
                                Modifier.weight(1f),
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            RadioButton(
                                selected = active,
                                onClick = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            "${money(plan.price)} ${if (plan.currency == "UZS") "so‘m" else plan.currency}",
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${plan.days} kunlik obuna", color = TextMuted, fontSize = 12.sp)
                            if (saving > 0)
                                Surface(
                                    color = PrimaryCyan.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    Text(
                                        "$saving% tejash",
                                        color = PrimaryCyan,
                                        fontSize = 11.sp,
                                        modifier =
                                            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    )
                                }
                        }
                    }
                }
            }
            state.errorMessage?.let {
                item { Box(Modifier.padding(horizontal = 20.dp)) { InlineMessage(it) } }
            }
            browserError?.let {
                item { Box(Modifier.padding(horizontal = 20.dp)) { InlineMessage(it) } }
            }
            state.paymentSuccessMessage?.let {
                item {
                    EmptyState(
                        "Premium faol",
                        "To‘lov tasdiqlandi. Yangi hikoyalarni tomosha qilishingiz mumkin.",
                        Icons.Outlined.CheckCircle,
                        "Davom etish",
                        onBack,
                    )
                }
            }
            if (order != null)
                item {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        InlineMessage(
                            "To‘lov natijasi kutilmoqda. Tasdiqlangach obunangiz avtomatik yangilanadi."
                        )
                        OutlinedButton(
                            onClick = { order?.let { id -> viewModel.checkPaymentStatus(id) {} } },
                            enabled = !state.isCheckingPayment,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (state.isCheckingPayment) "Tekshirilmoqda…"
                                else "To‘lovni tekshirish"
                            )
                        }
                    }
                }
        }
    }
}
