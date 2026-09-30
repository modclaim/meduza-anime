package uz.meduza.anime.ui.auth

import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.meduza.anime.R
import uz.meduza.anime.core.ui.components.PrimaryButton
import uz.meduza.anime.core.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var emailInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var isPrivacyAccepted by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }

    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && !hasNavigated) {
            hasNavigated = true
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(44.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_meduza_logo),
                contentDescription = "Meduza Logo",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Yangi hisob yaratish",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email manzili", color = TextGray) },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = InputBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = { Text("Foydalanuvchi nomi", color = TextGray) },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = InputBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Parol (kamida 6 ta belgi)", color = TextGray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = InputBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            // Privacy Policy Acceptance Checkbox Row
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isPrivacyAccepted,
                    onCheckedChange = { isPrivacyAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryCyan,
                        uncheckedColor = TextGray,
                        checkmarkColor = BackgroundDark
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                val annotatedText = buildAnnotatedString {
                    append("Men ")
                    pushStringAnnotation(tag = "PRIVACY", annotation = "open_privacy")
                    withStyle(
                        style = SpanStyle(
                            color = PrimaryCyan,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Maxfiylik siyosati va shartlari")
                    }
                    pop()
                    append("ga roziman")
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                            .firstOrNull()?.let {
                                showPrivacyModal = true
                            } ?: run {
                                isPrivacyAccepted = !isPrivacyAccepted
                            }
                    }
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = BadgeSeriesRed,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            PrimaryButton(
                text = "Akkaunt yaratish",
                onClick = {
                    if (emailInput.isNotBlank() && usernameInput.isNotBlank() && passwordInput.isNotBlank()) {
                        if (!isPrivacyAccepted) {
                            showPrivacyModal = true
                        } else {
                            viewModel.register(emailInput.trim(), usernameInput.trim(), passwordInput.trim())
                        }
                    }
                },
                isLoading = uiState.isLoading,
                enabled = emailInput.isNotBlank() && usernameInput.isNotBlank() && passwordInput.isNotBlank() && isPrivacyAccepted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Akkauntingiz bormi? Kirish",
                color = PrimaryCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Privacy Policy Modal Box with WebView & Close Button (X)
        if (showPrivacyModal) {
            Dialog(
                onDismissRequest = { showPrivacyModal = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.94f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                        border = BorderStroke(1.dp, InputBorder)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header with Title and "X" Close Button on Top Right
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardDarkElevated)
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🪼", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Maxfiylik shartlari",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                IconButton(
                                    onClick = { showPrivacyModal = false },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Chiqish",
                                        tint = TextWhite
                                    )
                                }
                            }

                            HorizontalDivider(color = InputBorder)

                            // Middle: WebView rendering the full Privacy Policy
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                var isWebViewLoading by remember { mutableStateOf(true) }

                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            setBackgroundColor(android.graphics.Color.parseColor("#0B0F17"))
                                            settings.apply {
                                                javaScriptEnabled = true
                                                domStorageEnabled = true
                                                cacheMode = WebSettings.LOAD_DEFAULT
                                                useWideViewPort = true
                                                loadWithOverviewMode = true
                                            }
                                            webViewClient = object : WebViewClient() {
                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                    isWebViewLoading = false
                                                }
                                                override fun onReceivedError(
                                                    view: WebView?,
                                                    errorCode: Int,
                                                    description: String?,
                                                    failingUrl: String?
                                                ) {
                                                    super.onReceivedError(view, errorCode, description, failingUrl)
                                                    isWebViewLoading = false
                                                }
                                            }
                                            loadUrl("https://meduza.editor.voiplay.uz/privacy")
                                        }
                                    }
                                )

                                if (isWebViewLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = PrimaryCyan,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = InputBorder)

                            // Bottom Action: "Roziman va qabul qilaman"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardDarkElevated)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showPrivacyModal = false }
                                ) {
                                    Text("Yopish", color = TextGray)
                                }

                                Button(
                                    onClick = {
                                        isPrivacyAccepted = true
                                        showPrivacyModal = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryCyan,
                                        contentColor = OnPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Roziman va qabul qilaman",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
