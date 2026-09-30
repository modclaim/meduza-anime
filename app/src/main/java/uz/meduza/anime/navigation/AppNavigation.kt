package uz.meduza.anime.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import uz.meduza.anime.core.ui.components.MeduzaBottomNav
import uz.meduza.anime.core.ui.theme.BackgroundDark
import uz.meduza.anime.ui.admin.AdminDashboardScreen
import uz.meduza.anime.ui.auth.LoginScreen
import uz.meduza.anime.ui.auth.RegisterScreen
import uz.meduza.anime.ui.detail.AnimeDetailScreen
import uz.meduza.anime.ui.explore.ExploreScreen
import uz.meduza.anime.ui.home.HomeScreen
import uz.meduza.anime.ui.library.LibraryScreen
import uz.meduza.anime.ui.player.VideoPlayerScreen
import uz.meduza.anime.ui.profile.ProfileScreen
import uz.meduza.anime.ui.schedule.ScheduleScreen
import uz.meduza.anime.ui.search.SearchScreen
import uz.meduza.anime.ui.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in bottomNavRoutes

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        Scaffold(
            // Disable Scaffold's own inset handling so each screen manages its own
            // status/navigation bar padding. This prevents double-padding.
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    MeduzaBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            containerColor = BackgroundDark
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(180)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(180)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) }
            ) {
                // Splash Screen (3-5s animated intro)
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = { loggedIn ->
                            val target = if (loggedIn) Screen.Home.route else Screen.Login.route
                            navController.navigate(target) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Auth Routes
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Main Bottom Tabs
                composable(Screen.Home.route) {
                    HomeScreen(
                        onAnimeClick = { slug, initialTab ->
                            navController.navigate(Screen.AnimeDetail.createRoute(slug, initialTab))
                        },
                        onEpisodeClick = { episodeId, slug ->
                            navController.navigate(Screen.Player.createRoute(episodeId, slug))
                        }
                    )
                }

                composable(Screen.Explore.route) {
                    ExploreScreen(
                        onAnimeClick = { slug ->
                            navController.navigate(Screen.AnimeDetail.createRoute(slug))
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onAnimeClick = { slug ->
                            navController.navigate(Screen.AnimeDetail.createRoute(slug))
                        }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        onAnimeClick = { slug ->
                            navController.navigate(Screen.AnimeDetail.createRoute(slug))
                        },
                        onEpisodeClick = { episodeId, slug ->
                            navController.navigate(Screen.Player.createRoute(episodeId, slug))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAdmin = {
                            navController.navigate(Screen.AdminDashboard.route)
                        },
                        onNavigateToFavorites = {
                            navController.navigate(Screen.Library.route)
                        }
                    )
                }

                composable(Screen.Schedule.route) {
                    ScheduleScreen(
                        onAnimeClick = { slug ->
                            navController.navigate(Screen.AnimeDetail.createRoute(slug))
                        }
                    )
                }

                // Detail Screen
                composable(
                    route = Screen.AnimeDetail.route,
                    arguments = listOf(
                        navArgument("slug") { type = NavType.StringType },
                        navArgument("initialTab") {
                            type = NavType.IntType
                            defaultValue = 0
                        }
                    )
                ) { backStackEntry ->
                    val slug = backStackEntry.arguments?.getString("slug") ?: ""
                    val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
                    AnimeDetailScreen(
                        slug = slug,
                        initialTab = initialTab,
                        onBackClick = { navController.popBackStack() },
                        onEpisodeClick = { episodeId, epSlug ->
                            navController.navigate(Screen.Player.createRoute(episodeId, epSlug))
                        }
                    )
                }

                // Video Player Screen — no innerPadding here, player manages its own insets
                composable(
                    route = Screen.Player.route,
                    arguments = listOf(
                        navArgument("episodeId") { type = NavType.StringType },
                        navArgument("slug") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                    val slug = backStackEntry.arguments?.getString("slug") ?: ""
                    VideoPlayerScreen(
                        episodeId = episodeId,
                        animeSlug = slug,
                        onBackClick = { navController.popBackStack() },
                        onUpgradePremiumClick = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                // Admin Dashboard
                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
