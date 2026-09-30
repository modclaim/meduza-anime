package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import uz.meduza.anime.core.ui.theme.BackgroundDark
import uz.meduza.anime.core.ui.theme.PrimaryCyan
import uz.meduza.anime.core.ui.theme.TextMuted
import uz.meduza.anime.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Asosiy", Icons.Default.Home)
    object Explore : BottomNavItem(Screen.Explore.route, "Katalog", Icons.Default.Explore)
    object Search : BottomNavItem(Screen.Search.route, "Qidiruv", Icons.Default.Search)
    object Library : BottomNavItem(Screen.Library.route, "Sevimlilar", Icons.Default.Favorite)
    object Profile : BottomNavItem(Screen.Profile.route, "Profil", Icons.Default.Person)
}

@Composable
fun MeduzaBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Search,
        BottomNavItem.Library,
        BottomNavItem.Profile
    )

    // navigationBarsPadding() ensures the bar clears system gesture bar /
    // home-indicator on both Android (3-button + gesture nav) and iOS (home indicator)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .navigationBarsPadding()
            .padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(item.route) }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (isSelected) PrimaryCyan else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(PrimaryCyan, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}
