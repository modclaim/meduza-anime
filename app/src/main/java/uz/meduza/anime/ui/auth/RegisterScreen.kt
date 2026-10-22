package uz.meduza.anime.ui.auth

import androidx.compose.runtime.Composable

// Legacy entry point retained for saved navigation states; all sign-in uses Nexa ID.
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    LoginScreen(
        onLoginSuccess = onRegisterSuccess,
        onContinueAsGuest = onNavigateToLogin,
        viewModel = viewModel,
    )
}
