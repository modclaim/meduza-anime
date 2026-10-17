package uz.meduza.anime.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uz.meduza.anime.MeduzaBackground
import uz.meduza.anime.MeduzaCard
import uz.meduza.anime.MeduzaCardElevated
import uz.meduza.anime.MeduzaCyan
import uz.meduza.anime.MeduzaGold
import uz.meduza.anime.MeduzaRed
import uz.meduza.anime.MeduzaSurface
import uz.meduza.anime.MeduzaTextGray
import uz.meduza.anime.MeduzaTextMuted
import uz.meduza.anime.MeduzaTextWhite
import uz.meduza.anime.core.auth.AuthManager
import uz.meduza.anime.core.auth.model.AuthState

@Composable
fun ProfileAuthScreen(
    authManager: AuthManager,
    modifier: Modifier = Modifier
) {
    val authState by authManager.authState.collectAsState()
    val scope = rememberCoroutineScope()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Notification banner if any
        if (snackbarMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MeduzaCyan.copy(alpha = 0.2f))
                        .border(1.dp, MeduzaCyan, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = snackbarMessage ?: "",
                            color = MeduzaTextWhite,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "✕",
                            color = MeduzaTextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { snackbarMessage = null }
                        )
                    }
                }
            }
        }

        when (val state = authState) {
            is AuthState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MeduzaCard)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MeduzaCyan,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nexa ID bilan bog'lanilmoqda...",
                                color = MeduzaTextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Iltimos kuting, xavfsiz seans tekshirilmoqda",
                                color = MeduzaTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            is AuthState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MeduzaCard)
                            .border(1.dp, MeduzaRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚠️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Autentifikatsiyada xatolik",
                                    color = MeduzaRed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = MeduzaTextGray,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        authManager.signIn()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeduzaRed)
                            ) {
                                Text(text = "Qayta urinish", color = Color.White)
                            }
                        }
                    }
                }
            }

            is AuthState.Unauthenticated -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MeduzaCard)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MeduzaCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🔐", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Nexa ID",
                                        color = MeduzaTextWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Markazlashtirilgan OIDC Autentifikatsiyasi",
                                        color = MeduzaCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Meduza Anime platformasiga kirish va sevimli animelaringizni qurilmalararo sinxronlashtirish uchun Nexa hisobingiz bilan tizimga kiring.",
                                color = MeduzaTextGray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Security Specs Summary
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MeduzaBackground)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SecuritySpecRow(label = "Protokol", value = "OpenID Connect + OAuth 2.0")
                                SecuritySpecRow(label = "PKCE", value = "RFC 7636 (S256 Majburiy)")
                                SecuritySpecRow(label = "Brauzer", value = "Custom Tabs (Android) / ASWebAuth (iOS)")
                                SecuritySpecRow(label = "Xavfsiz Xotira", value = "EncryptedSharedPrefs / Keychain")
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        val result = authManager.signIn()
                                        if (result.isSuccess) {
                                            snackbarMessage = "Tizimga muvaffaqiyatli kirildi!"
                                        } else {
                                            snackbarMessage = "Kirishda xatolik: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeduzaCyan)
                            ) {
                                Text(
                                    text = "Nexa ID orqali kirish",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            is AuthState.Authenticated -> {
                // User Profile Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MeduzaCard)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val initial = state.user?.name?.take(1)
                                    ?: state.user?.username?.take(1)
                                    ?: state.user?.email?.take(1)
                                    ?: "U"

                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(MeduzaCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial.uppercase(),
                                        color = Color.Black,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.user?.name ?: state.user?.username ?: "Nexa Foydalanuvchisi",
                                        color = MeduzaTextWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.user?.email ?: "Email ko'rsatilmagan",
                                        color = MeduzaTextGray,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Faol seans (Nexa ID OIDC)",
                                            color = Color(0xFF00E676),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Token & Session Details Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MeduzaCard)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "XAVFSIZLIK VA TOKEN MA'LUMOTLARI",
                                color = MeduzaTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            SecuritySpecRow(
                                label = "User ID (sub)",
                                value = state.user?.sub?.take(16) ?: "Mavjud emas"
                            )
                            SecuritySpecRow(
                                label = "Access Token",
                                value = state.tokens.accessToken.take(18) + "..."
                            )
                            SecuritySpecRow(
                                label = "Refresh Token",
                                value = if (state.tokens.refreshToken != null) "Mavjud (offline_access)" else "Yo'q"
                            )
                            SecuritySpecRow(
                                label = "Token Turi",
                                value = state.tokens.tokenType
                            )
                            SecuritySpecRow(
                                label = "Token Muddati",
                                value = if (state.tokens.isExpired()) "Muddati tugagan" else "Amalda"
                            )
                        }
                    }
                }

                // Action Buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Refresh Token Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val token = authManager.getValidAccessToken()
                                    snackbarMessage = if (token != null) {
                                        "Token muvaffaqiyatli yangilandi!"
                                    } else {
                                        "Tokenni yangilab bo'lmadi"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MeduzaCyan
                            )
                        ) {
                            Text(
                                text = "🔄 Tokenni yangilash (Refresh)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        // Logout Button (Local)
                        Button(
                            onClick = {
                                scope.launch {
                                    authManager.signOut(launchBrowserEndSession = false)
                                    snackbarMessage = "Hisobdan chiqildi"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MeduzaCardElevated)
                        ) {
                            Text(
                                text = "🚪 Chiqish (Sign Out)",
                                color = MeduzaTextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        // Full Browser Logout (SLO)
                        TextButton(
                            onClick = {
                                scope.launch {
                                    authManager.signOut(launchBrowserEndSession = true)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Brauzerdagi barcha sessiyani tugatish (SLO)",
                                color = MeduzaTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecuritySpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MeduzaTextMuted,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MeduzaTextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
