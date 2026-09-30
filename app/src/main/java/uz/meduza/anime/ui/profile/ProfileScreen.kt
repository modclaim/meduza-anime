package uz.meduza.anime.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import uz.meduza.anime.data.models.PaymentPlanDto
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.core.ui.components.PrimaryButton
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.data.models.DeviceNotificationRequest
import java.io.File
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPremiumModal by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.paymentSuccessMessage) {
        uiState.paymentSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearPaymentSuccessMessage()
            showPremiumModal = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        } else if (uiState.user != null) {
            val user = uiState.user!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(CardDarkElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.avatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.username.take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Username & Role
                val displayName = if (user.username.contains("@")) {
                    user.username.substringBefore("@")
                } else {
                    user.username
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (user.isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PremiumGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VIP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Premium Banner / Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Premium",
                                    tint = if (user.isPremium) PremiumGold else PrimaryCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (user.isPremium) "Premium Faol" else "Premium Obuna",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                            }
                            if (!user.isPremium) {
                                Button(
                                    onClick = { showPremiumModal = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryCyan,
                                        contentColor = OnPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Faollashtirish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (user.isPremium && user.premiumExpiresAt != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tugash muddati: ${user.premiumExpiresAt.take(10)}",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Barcha yangi premyeralar, 1080p sifat va cheklovlarsiz shaxsiy dublyajlar!",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User Statistics Grid
                val count = user.count
                val bookmarksCount = count?.bookmarks ?: 0
                val historyCount = count?.watchHistory ?: 0
                val commentsCount = count?.comments ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "$bookmarksCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                            Text(text = "Saqlangan", fontSize = 11.sp, color = TextGray)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "$commentsCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                            Text(text = "Izohlar", fontSize = 11.sp, color = TextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Menu Items
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "Sevimlilar",
                    subtitle = "Saqlangan animelar ro'yxati",
                    onClick = onNavigateToFavorites
                )
                if (!user.isPremium) {
                    ProfileMenuItem(
                        icon = Icons.Default.Star,
                        title = "VIP Premium obuna",
                        subtitle = "Faollashtirish",
                        onClick = { showPremiumModal = true }
                    )
                }
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Ilova sozlamalari",
                    subtitle = "Bildirishnomalar, keshni tozalash",
                    onClick = { showSettingsDialog = true }
                )

                // Admin Dashboard Link (Only visible if user.role == "ADMIN")
                if (user.role.equals("ADMIN", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = OnPrimary
                        )
                    ) {
                        Text("🛡️ Admin Paneliga O'tish", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Logout Button
                OutlinedButton(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BadgeSeriesRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BadgeSeriesRed.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Chiqish", tint = BadgeSeriesRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tizimdan chiqish", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://meduza.editor.voiplay.uz/privacy"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Maxfiylik siyosati", color = TextGray, fontSize = 13.sp)
                    }

                    TextButton(
                        onClick = { showDeleteAccountDialog = true }
                    ) {
                        Text("Hisobni o'chirish", color = BadgeSeriesRed.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        } else {
            // Error / Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "Profil ma'lumotlarini yuklab bo'lmadi",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Qayta yuklash", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { viewModel.logout(onLogout) }
                    ) {
                        Text("Tizimdan chiqish", color = BadgeSeriesRed)
                    }
                }
            }
        }
    }

    // Upgrade Premium inPAY Gateway Modal
    if (showPremiumModal) {
        PaymentModalDialog(
            viewModel = viewModel,
            onDismiss = { showPremiumModal = false }
        )
    }

    // App Settings Modal (Cache & Notifications)
    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    text = "Hisobni butunlay o'chirish",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Haqiqatan ham hisobingizni o'chirib tashlamoqchimisiz? Ushbu amal qaytarib bo'lmaydi. Barcha tomosha tarixi, saqlangan animelar va faol obunalar butunlay o'chiriladi.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount {
                            Toast.makeText(context, "Hisobingiz muvaffaqiyatli o'chirildi", Toast.LENGTH_LONG).show()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BadgeSeriesRed)
                ) {
                    Text("O'chirish", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Bekor qilish", color = TextGray)
                }
            },
            containerColor = CardDarkElevated
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardDarkElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager.getInstance(context) }
    var notificationsEnabled by remember { mutableStateOf(sessionManager.areNotificationsEnabled()) }
    var cacheSizeText by remember { mutableStateOf("0.0 MB") }

    fun calculateCache() {
        val total = getFolderSize(context.cacheDir) + getFolderSize(context.externalCacheDir)
        cacheSizeText = formatSize(total)
    }

    LaunchedEffect(Unit) {
        calculateCache()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsEnabled = isGranted
        sessionManager.setNotificationsEnabled(isGranted)
        if (isGranted) {
            Toast.makeText(context, "Bildirishnomalar yoqildi", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bildirishnoma ruxsati berilmadi", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text("⚙️ Ilova sozlamalari", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bildirishnomalar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardDarkElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "Bildirishnomalar",
                                tint = PrimaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bildirishnomalar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (notificationsEnabled) "Yangi qismlar xabari yoqilgan" else "Xabarlar o'chirilgan",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        notificationsEnabled = true
                                        sessionManager.setNotificationsEnabled(true)
                                        scope.launch(Dispatchers.IO) {
                                            runCatching {
                                                val devName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
                                                ApiClient.getService(context).updateDeviceNotification(
                                                    DeviceNotificationRequest(deviceName = devName, enabled = true)
                                                )
                                            }
                                        }
                                        Toast.makeText(context, "Bildirishnomalar faol", Toast.LENGTH_SHORT).show()
                                    } else {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    notificationsEnabled = true
                                    sessionManager.setNotificationsEnabled(true)
                                    scope.launch(Dispatchers.IO) {
                                        runCatching {
                                            val devName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
                                            ApiClient.getService(context).updateDeviceNotification(
                                                DeviceNotificationRequest(deviceName = devName, enabled = true)
                                            )
                                        }
                                    }
                                    Toast.makeText(context, "Bildirishnomalar faol", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                notificationsEnabled = false
                                sessionManager.setNotificationsEnabled(false)
                                scope.launch(Dispatchers.IO) {
                                    runCatching {
                                        val devName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
                                        ApiClient.getService(context).updateDeviceNotification(
                                            DeviceNotificationRequest(deviceName = devName, enabled = false)
                                        )
                                    }
                                }
                                Toast.makeText(context, "Bildirishnomalar o'chirildi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryCyan,
                            checkedTrackColor = PrimaryCyan.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = CardDarkElevated
                        )
                    )
                }

                HorizontalDivider(color = InputBorder.copy(alpha = 0.5f))

                // Keshni tozalash
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardDarkElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Kesh",
                                tint = PrimaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kesh xotirasi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Band qilingan: $cacheSizeText",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    context.externalCacheDir?.deleteRecursively()
                                } catch (_: Exception) {}
                                withContext(Dispatchers.Main) {
                                    calculateCache()
                                    Toast.makeText(context, "Kesh muvaffaqiyatli tozalandi", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Tozalash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = InputBorder.copy(alpha = 0.5f))

                // App version
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ilova versiyasi", fontSize = 12.sp, color = TextGray)
                    Text("v1.0.1 (Rasmiy)", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = OnPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tayyor", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0.0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.1f MB", mb)
    } else {
        String.format(Locale.US, "%.1f KB", kb)
    }
}

private fun getFolderSize(dir: File?): Long {
    if (dir == null || !dir.exists()) return 0L
    var size = 0L
    val files = dir.listFiles() ?: return 0L
    for (f in files) {
        size += if (f.isDirectory) getFolderSize(f) else f.length()
    }
    return size
}

@Composable
fun PaymentModalDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val plans = if (uiState.plans.isNotEmpty()) uiState.plans else listOf(
        PaymentPlanDto("1_MONTH", "1 Oylik Premium", 30, 25000, "UZS", null, "1080p FHD sifat, barcha qismlar, reklamasiz"),
        PaymentPlanDto("3_MONTHS", "3 Oylik Premium", 90, 70000, "UZS", "7% chegirma", "3 oy cheklovlarsiz tomosha va to'liq kirish"),
        PaymentPlanDto("6_MONTHS", "6 Oylik Premium", 180, 130000, "UZS", "13% chegirma", "Yarim yillik qulay obuna va yangi premyeralar"),
        PaymentPlanDto("12_MONTHS", "1 Yillik VIP Premium", 365, 230000, "UZS", "23% chegirma", "1 yil to'liq VIP obuna, erta premyeralar va eksklyuziv sifat")
    )
    var selectedPlanType by remember { mutableStateOf(plans.firstOrNull()?.planType ?: "1_MONTH") }
    var currentOrderId by remember { mutableStateOf<String?>(null) }
    var hasOpenedBrowser by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.plans) {
        if (uiState.plans.isNotEmpty() && uiState.plans.none { it.planType == selectedPlanType }) {
            selectedPlanType = uiState.plans.first().planType
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Premium",
                        tint = PremiumGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Meduza Premium Obuna",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "inPAY to'lov tizimi (Click, Payme, Uzcard, Humo)",
                    color = PrimaryCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Features
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardDarkElevated
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1080p Full HD va original sifat", color = TextWhite, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Barcha qismlar va yangi premyeralar", color = TextWhite, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚫", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reklamasiz to'liq qulay tomosha", color = TextWhite, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VIP profil nishoni", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tarifni tanlang:",
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Plans list
                plans.forEach { plan ->
                    val isSelected = plan.planType == selectedPlanType
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedPlanType = plan.planType },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) PrimaryCyan.copy(alpha = 0.15f) else CardDarkElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) PrimaryCyan else Color.White.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plan.title,
                                        color = if (isSelected) PrimaryCyan else TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (plan.discount != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = BadgeSeriesRed.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = plan.discount,
                                                color = BadgeSeriesRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = plan.description,
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "${"%,d".format(Locale.US, plan.price).replace(',', ' ')} so'm",
                                color = if (isSelected) PremiumGold else TextWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Payment providers branding badge
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "To'lov usullari: Click • Payme • Uzcard • Humo",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                if (hasOpenedBrowser && currentOrderId != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CardDarkElevated
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "To'lov sahifasi ochildi. To'lovni bajargach quyidagi tugmani bosing:",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    currentOrderId?.let { orderId ->
                                        viewModel.checkPaymentStatus(orderId) { success ->
                                            if (!success) {
                                                Toast.makeText(
                                                    context,
                                                    "To'lov hali amalga oshirilmagan yoki kutilmoqda",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !uiState.isCheckingPayment
                            ) {
                                if (uiState.isCheckingPayment) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tekshirilmoqda...", fontSize = 12.sp)
                                } else {
                                    Text("✅ To'lovni tekshirish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    currentOrderId?.let { orderId ->
                                        viewModel.simulateTestPayment(orderId) { success ->
                                            if (success) {
                                                Toast.makeText(context, "Premium faollashtirildi!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumGold),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PremiumGold.copy(alpha = 0.5f))
                            ) {
                                Text("⚡ Demo test orqali tasdiqlash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.createPayment(selectedPlanType) { payUrl, orderId ->
                        currentOrderId = orderId
                        hasOpenedBrowser = true
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Brauzerni ochib bo'lmadi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !uiState.isCreatingPayment,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isCreatingPayment) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yuklanmoqda...")
                } else {
                    Text("inPAY orqali to'lash", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = TextGray)
            }
        }
    )
}
