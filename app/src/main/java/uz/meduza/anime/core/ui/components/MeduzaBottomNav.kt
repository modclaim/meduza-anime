package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.navigation.Screen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Asosiy", Icons.Outlined.Home)

    object Explore : BottomNavItem(Screen.Explore.route, "Katalog", Icons.Outlined.GridView)

    object Search : BottomNavItem(Screen.Search.route, "Qidiruv", Icons.Outlined.Search)

    object Library : BottomNavItem(Screen.Library.route, "Saqlangan", Icons.Outlined.BookmarkBorder)

    object Profile : BottomNavItem(Screen.Profile.route, "Profil", Icons.Outlined.PersonOutline)
}

@Composable
fun MeduzaBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(SurfaceDark).navigationBarsPadding()) {
        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
        Row(
            Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Explore,
                    BottomNavItem.Search,
                    BottomNavItem.Library,
                    BottomNavItem.Profile,
                )
                .forEach { item ->
                    val selected = currentRoute == item.route
                    TextButton(
                        onClick = { onNavigate(item.route) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                item.icon,
                                null,
                                Modifier.size(23.dp),
                                tint = if (selected) PrimaryCyan else TextMuted,
                            )
                            Text(
                                item.title,
                                fontSize = 10.sp,
                                color = if (selected) TextWhite else TextMuted,
                                maxLines = 1,
                            )
                        }
                    }
                }
        }
    }
}
