package uz.meduza.anime.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uz.meduza.anime.core.config.*
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.ui.admin.AdminDashboardScreen
import uz.meduza.anime.ui.auth.LoginScreen
import uz.meduza.anime.ui.detail.AnimeDetailScreen
import uz.meduza.anime.ui.explore.ExploreScreen
import uz.meduza.anime.ui.home.HomeScreen
import uz.meduza.anime.ui.library.LibraryScreen
import uz.meduza.anime.ui.player.VideoPlayerScreen
import uz.meduza.anime.ui.profile.PremiumScreen
import uz.meduza.anime.ui.profile.ProfileScreen
import uz.meduza.anime.ui.schedule.ScheduleScreen
import uz.meduza.anime.ui.search.SearchScreen
import uz.meduza.anime.ui.splash.SplashScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val session = remember { SessionManager.getInstance(context) }
    val loggedIn by session.isLoggedIn.collectAsState(initial = session.isLoggedInSync())
    val store = remember { AppConfigStore.getInstance(context) }
    val config by store.config.collectAsState()
    val lifecycle = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var guestSession by rememberSaveable { mutableStateOf(false) }
    var requireLogin by remember { mutableStateOf(false) }
    val mainTabs =
        listOf(
            Screen.Home.route,
            Screen.Explore.route,
            Screen.Search.route,
            Screen.Library.route,
            Screen.Profile.route,
        )
    fun login() {
        nav.navigate(Screen.Login.route) { launchSingleTop = true }
    }
    fun home() {
        nav.navigate(Screen.Home.route) {
            popUpTo(0)
            launchSingleTop = true
        }
    }
    fun play(episodeId: String, slug: String) {
        if (AccessPolicy.canPlay(loggedIn, config))
            nav.navigate(Screen.Player.createRoute(episodeId, slug))
        else requireLogin = true
    }
    LaunchedEffect(lifecycle) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                store.refresh()
                delay(30_000)
            }
        }
    }
    LaunchedEffect(loggedIn, config.guestEnabled, route) {
        if (loggedIn) guestSession = false
        if (
            !loggedIn &&
                route != null &&
                route !in listOf(Screen.Login.route, Screen.Register.route, Screen.Splash.route) &&
                (!guestSession || !config.guestEnabled)
        ) {
            guestSession = false
            nav.navigate(Screen.Login.route) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }
    // Removing the player from composition immediately pauses/releases playback during maintenance.
    if (config.maintenanceEnabled) {
        Surface(Modifier.fillMaxSize(), color = BackgroundDark) {
            Box(
                Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    config.maintenanceTitle,
                    config.maintenanceMessage,
                    Icons.Outlined.Construction,
                    "Qayta tekshirish",
                    { scope.launch { store.refresh() } },
                )
            }
        }
        return
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = BackgroundDark,
        bottomBar = {
            if (route in mainTabs)
                MeduzaBottomNav(
                    route,
                    { target ->
                        if (route != target)
                            nav.navigate(target) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                    },
                )
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize().padding(padding),
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(150)) },
        ) {
            composable(Screen.Splash.route) {
                SplashScreen { active ->
                    nav.navigate(if (active) Screen.Home.route else Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        guestSession = false
                        home()
                    },
                    onContinueAsGuest = {
                        if (AccessPolicy.canBrowseAsGuest(config)) {
                            guestSession = true
                            home()
                        }
                    },
                    guestEnabled = config.guestEnabled,
                )
            }
            composable(Screen.Register.route) { LaunchedEffect(Unit) { login() } }
            composable(Screen.Home.route) {
                HomeScreen(
                    onAnimeClick = { slug, tab ->
                        nav.navigate(
                            Screen.AnimeDetail.createRoute(slug, if (!loggedIn) 1 else tab)
                        )
                    },
                    onEpisodeClick = ::play,
                    onSearchClick = { nav.navigate(Screen.Search.route) },
                    onScheduleClick = { nav.navigate(Screen.Schedule.route) },
                    onPlayClick = { slug ->
                        if (loggedIn) nav.navigate(Screen.AnimeDetail.createRoute(slug))
                        else requireLogin = true
                    },
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    onAnimeClick = { slug ->
                        nav.navigate(Screen.AnimeDetail.createRoute(slug, if (loggedIn) 0 else 1))
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onAnimeClick = { slug ->
                        nav.navigate(Screen.AnimeDetail.createRoute(slug, if (loggedIn) 0 else 1))
                    }
                )
            }
            composable(Screen.Library.route) {
                if (loggedIn)
                    LibraryScreen(
                        onAnimeClick = { nav.navigate(Screen.AnimeDetail.createRoute(it)) },
                        onEpisodeClick = ::play,
                    )
                else
                    Box(
                        Modifier.fillMaxSize().statusBarsPadding(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            "Shaxsiy kutubxonangiz",
                            "Animelarni saqlash va ko‘rish tarixini davom ettirish uchun hisobingizga kiring.",
                            Icons.Outlined.Lock,
                            "Nexa ID orqali kirish",
                            ::login,
                        )
                    }
            }
            composable(Screen.Profile.route) {
                if (!loggedIn)
                    Surface(Modifier.fillMaxSize(), color = BackgroundDark) {
                        Column(
                            Modifier.fillMaxSize().safeDrawingPadding().padding(28.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Siz mehmon sifatida kirdingiz",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(Modifier.height(20.dp))
                            TextButton(
                                onClick = {
                                    guestSession = false
                                    nav.navigate(Screen.Login.route) {
                                        popUpTo(0)
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Text("Chiqish")
                            }
                        }
                    }
                else
                    ProfileScreen(
                        onLogout = {
                            guestSession = false
                            nav.navigate(Screen.Login.route) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAdmin = { nav.navigate(Screen.AdminDashboard.route) },
                        onNavigateToFavorites = { nav.navigate(Screen.Library.route) },
                        onNavigateToPremium = { nav.navigate(Screen.Premium.route) },
                    )
            }
            composable(Screen.Premium.route) { PremiumScreen(onBack = { nav.popBackStack() }) }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onAnimeClick = {
                        nav.navigate(Screen.AnimeDetail.createRoute(it, if (loggedIn) 0 else 1))
                    }
                )
            }
            composable(
                Screen.AnimeDetail.route,
                arguments =
                    listOf(
                        navArgument("slug") { type = NavType.StringType },
                        navArgument("initialTab") {
                            type = NavType.IntType
                            defaultValue = 0
                        },
                    ),
            ) { backStack ->
                AnimeDetailScreen(
                    slug = backStack.arguments?.getString("slug") ?: "",
                    initialTab = backStack.arguments?.getInt("initialTab") ?: 0,
                    onBackClick = { nav.popBackStack() },
                    onEpisodeClick = ::play,
                    isLoggedIn = loggedIn,
                    onSignIn = { requireLogin = true },
                )
            }
            composable(
                Screen.Player.route,
                arguments =
                    listOf(
                        navArgument("episodeId") { type = NavType.StringType },
                        navArgument("slug") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                    ),
            ) { backStack ->
                if (AccessPolicy.canPlay(loggedIn, config))
                    VideoPlayerScreen(
                        episodeId = backStack.arguments?.getString("episodeId") ?: "",
                        animeSlug = backStack.arguments?.getString("slug") ?: "",
                        onBackClick = { nav.popBackStack() },
                        onUpgradePremiumClick = { nav.navigate(Screen.Premium.route) },
                    )
                else
                    LaunchedEffect(Unit) {
                        nav.popBackStack()
                        requireLogin = true
                    }
            }
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(onBackClick = { nav.popBackStack() })
            }
        }
    }
    if (requireLogin)
        AlertDialog(
            onDismissRequest = { requireLogin = false },
            icon = { Icon(Icons.Outlined.Lock, null, tint = PrimaryCyan) },
            title = { Text("Tomosha qilish uchun kiring") },
            text = {
                Text(
                    "Qismlar, baholash va shaxsiy kutubxona hisobingizga kirgandan keyin ochiladi."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        requireLogin = false
                        login()
                    }
                ) {
                    Text("Nexa ID orqali kirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { requireLogin = false }) { Text("Hozir emas") }
            },
        )
}
