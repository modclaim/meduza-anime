package uz.meduza.anime.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.meduza.anime.R
import uz.meduza.anime.core.ui.components.LegalDocumentDialog
import uz.meduza.anime.core.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    guestEnabled: Boolean = true,
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var document by remember { mutableStateOf<String?>(null) }
    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && !navigated) {
            navigated = true
            viewModel.resetState()
            onLoginSuccess()
        }
    }
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF142A28), BackgroundDark, BackgroundDark))
            )
    ) {
        Column(
            Modifier.fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(52.dp))
            Image(painterResource(R.drawable.ic_meduza_logo), "Meduza", Modifier.size(76.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                "MEDUZA",
                color = TextWhite,
                fontSize = 20.sp,
                letterSpacing = 5.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(64.dp))
            Text(
                "Yaxshi hikoyalar.\nO‘z tilingizda.",
                color = TextWhite,
                fontSize = 34.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Sevimli animelaringiz, professional dublyaj va yangi premyeralar — barchasi bir joyda.",
                color = TextGray,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = viewModel::signInWithNexa,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                if (state.isLoading)
                    CircularProgressIndicator(
                        Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = OnPrimary,
                    )
                else {
                    Text(
                        "Nexa ID orqali kirish",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
                }
            }
            if (guestEnabled) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onContinueAsGuest,
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Icon(Icons.Outlined.Explore, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Mehmon sifatida ko‘rish", color = TextWhite)
                }
            }
            state.errorMessage?.let {
                Text(
                    it,
                    color = BadgeSeriesRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            Spacer(Modifier.height(38.dp))
            Text(
                "Davom etish orqali foydalanish shartlari va maxfiylik siyosatiga rozilik bildirasiz.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    onClick = { document = "Maxfiylik siyosati" },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text("Maxfiylik", fontSize = 12.sp)
                }
                TextButton(
                    onClick = { document = "Foydalanish shartlari" },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text("Foydalanish shartlari", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    document?.let { title ->
        LegalDocumentDialog(
            title,
            "https://meduza.nexa.uz/" +
                if (title == "Foydalanish shartlari") "terms" else "privacy",
            onDismiss = { document = null },
        )
    }
}
