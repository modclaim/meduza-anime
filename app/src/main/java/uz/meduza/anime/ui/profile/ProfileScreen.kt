package uz.meduza.anime.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.meduza.anime.BuildConfig
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var document by remember { mutableStateOf<String?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmCache by remember { mutableStateOf(false) }
    var cacheBusy by remember { mutableStateOf(false) }
    var cacheMessage by remember { mutableStateOf<String?>(null) }
    val requestNotifications =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
            cacheMessage =
                if (allowed) "Bildirishnomalar yoqildi" else "Bildirishnomalarga ruxsat berilmadi"
        }
    Column(
        Modifier.fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        ScreenHeading("Profil", "Hammasi sizga moslangan")
        if (state.isLoading)
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        else if (state.user == null)
            EmptyState(
                "Profil yuklanmadi",
                "Internet aloqasini tekshirib, qayta urinib ko‘ring.",
                actionLabel = "Qayta urinish",
                onAction = { viewModel.loadProfile(true) },
            )
        state.user?.let { user ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    Modifier.size(66.dp).clip(CircleShape).background(CardDarkElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!user.avatar.isNullOrBlank())
                        AsyncImage(
                            user.avatar,
                            null,
                            Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    else
                        Text(
                            user.username.take(1).uppercase(),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan,
                        )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        user.username,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                    )
                    Text(
                        if (user.isPremium) "Premium hisob" else "Shaxsiy hisob",
                        color = if (user.isPremium) PremiumGold else TextMuted,
                        fontSize = 13.sp,
                    )
                }
            }
            Surface(
                onClick = onNavigateToPremium,
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        Icons.Outlined.WorkspacePremium,
                        null,
                        tint = PremiumGold,
                        modifier = Modifier.size(29.dp),
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (user.isPremium) "Premium faol" else "Meduza Premium",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (user.isPremium)
                                user.premiumExpiresAt?.take(10)?.let { "Amal qilish muddati: $it" }
                                    ?: "Obunangizni boshqaring"
                            else "Ko‘proq hikoyalar. Cheklovlarsiz tomosha.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextMuted)
                }
            }
            Column(
                Modifier.padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
            ) {
                ProfileRow(
                    Icons.Outlined.BookmarkBorder,
                    "Saqlanganlar",
                    "Shaxsiy anime kutubxonangiz",
                    onNavigateToFavorites,
                )
                HorizontalDivider(
                    color = DividerDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ProfileRow(
                    Icons.Outlined.NotificationsNone,
                    "Bildirishnomalar",
                    "Yangi qismlar haqida xabarlar",
                    {
                        if (
                            Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != PackageManager.PERMISSION_GRANTED
                        )
                            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else {
                            val intent =
                                android.content
                                    .Intent(
                                        android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    )
                                    .putExtra(
                                        android.provider.Settings.EXTRA_APP_PACKAGE,
                                        context.packageName,
                                    )
                            runCatching { context.startActivity(intent) }
                        }
                    },
                )
                HorizontalDivider(
                    color = DividerDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ProfileRow(
                    Icons.Outlined.Storage,
                    "Keshni tozalash",
                    "Hisob va tarix saqlanadi",
                    { confirmCache = true },
                )
                if (user.role == "ADMIN") {
                    HorizontalDivider(
                        color = DividerDark,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    ProfileRow(
                        Icons.Outlined.AdminPanelSettings,
                        "Boshqaruv",
                        "Administrator imkoniyatlari",
                        onNavigateToAdmin,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier.padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
            ) {
                ProfileRow(
                    Icons.Outlined.Description,
                    "Foydalanish shartlari",
                    null,
                    { document = "Foydalanish shartlari" },
                )
                HorizontalDivider(
                    color = DividerDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ProfileRow(
                    Icons.Outlined.PrivacyTip,
                    "Maxfiylik siyosati",
                    null,
                    { document = "Maxfiylik siyosati" },
                )
            }
            Spacer(Modifier.height(20.dp))
            TextButton(
                onClick = { confirmLogout = true },
                modifier =
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Tizimdan chiqish")
            }
            TextButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Hisobni o‘chirish", color = TextMuted, fontSize = 12.sp)
            }
        }
        cacheMessage?.let {
            Text(
                it,
                color = PrimaryCyan,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        state.errorMessage?.let { Box(Modifier.padding(20.dp)) { InlineMessage(it) } }
        Text(
            "Meduza ${BuildConfig.VERSION_NAME}",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp),
        )
    }
    document?.let {
        LegalDocumentDialog(
            it,
            "https://meduza.nexa.uz/" + if (it == "Foydalanish shartlari") "terms" else "privacy",
            { document = null },
        )
    }
    if (confirmLogout)
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Hisobdan chiqasizmi?") },
            text = {
                Text("Sevimlilar va tarix hisobingizda qoladi. Keyin qayta kirishingiz mumkin.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text("Chiqish")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Bekor qilish") }
            },
        )
    if (confirmCache)
        AlertDialog(
            onDismissRequest = { if (!cacheBusy) confirmCache = false },
            title = { Text("Keshni tozalash") },
            text = {
                Text("Vaqtinchalik rasm va fayllar o‘chiriladi. Seans va ko‘rish tarixi saqlanadi.")
            },
            confirmButton = {
                TextButton(
                    enabled = !cacheBusy,
                    onClick = {
                        cacheBusy = true
                        scope.launch {
                            val ok =
                                withContext(Dispatchers.IO) {
                                    runCatching {
                                        context.cacheDir.listFiles()?.forEach {
                                            it.deleteRecursively()
                                        }
                                    }
                                        .isSuccess
                                }
                            cacheBusy = false
                            confirmCache = false
                            cacheMessage = if (ok) "Kesh tozalandi" else "Keshni tozalab bo‘lmadi"
                        }
                    },
                ) {
                    Text(if (cacheBusy) "Tozalanmoqda…" else "Tozalash")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCache = false }, enabled = !cacheBusy) {
                    Text("Bekor qilish")
                }
            },
        )
    if (confirmDelete) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hisobni butunlay o‘chirish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Meduza hisobingiz va unga bog‘langan ma’lumotlar o‘chiriladi. Bu amalni qaytarib bo‘lmaydi. Tasdiqlash uchun O‘CHIRISH deb yozing."
                    )
                    OutlinedTextField(
                        text,
                        { text = it },
                        singleLine = true,
                        label = { Text("O‘CHIRISH") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = text.uppercase().replace('’', '\'').replace('‘', '\'') == "O'CHIRISH",
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteAccount(onLogout)
                    },
                ) {
                    Text("Hisobni o‘chirish", color = BadgeSeriesRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Bekor qilish") }
            },
        )
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    Surface(onClick = onClick, color = SurfaceDark) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, null, tint = TextGray, modifier = Modifier.size(21.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                subtitle?.let { Text(it, color = TextMuted, fontSize = 11.sp) }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
