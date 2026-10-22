package uz.meduza.anime.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")

    object Login : Screen("auth/login")

    object Register : Screen("auth/register")

    object Home : Screen("main/home")

    object Explore : Screen("main/explore")

    object Search : Screen("main/search")

    object Library : Screen("main/library")

    object Profile : Screen("main/profile")

    object Premium : Screen("account/premium")

    object Schedule : Screen("main/schedule")

    object AnimeDetail : Screen("anime/{slug}?initialTab={initialTab}") {
        fun createRoute(slug: String, initialTab: Int = 0) =
            "anime/${android.net.Uri.encode(slug)}?initialTab=$initialTab"
    }

    object Player : Screen("player/{episodeId}?slug={slug}") {
        fun createRoute(episodeId: String, slug: String = "") =
            "player/${android.net.Uri.encode(episodeId)}?slug=${android.net.Uri.encode(slug)}"
    }

    object AdminDashboard : Screen("admin/dashboard")
}
