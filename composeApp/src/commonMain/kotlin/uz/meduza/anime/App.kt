package uz.meduza.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Meduza Anime Theme Tokens
val MeduzaBackground = Color(0xFF0F1015)
val MeduzaSurface = Color(0xFF16171F)
val MeduzaCard = Color(0xFF1F222E)
val MeduzaCardElevated = Color(0xFF282B3A)
val MeduzaCyan = Color(0xFF00E5BE)
val MeduzaCyanLight = Color(0xFF64FFDA)
val MeduzaGold = Color(0xFFFFD700)
val MeduzaRed = Color(0xFFFF4D4D)
val MeduzaTextWhite = Color(0xFFFFFFFF)
val MeduzaTextGray = Color(0xFF9E9E9E)
val MeduzaTextMuted = Color(0xFF6B6E7D)

data class AnimeItem(
    val id: String,
    val title: String,
    val episodes: String,
    val rating: String,
    val type: String,
    val year: String
)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = MeduzaBackground,
            surface = MeduzaSurface,
            primary = MeduzaCyan,
            onPrimary = Color.Black,
            onBackground = MeduzaTextWhite,
            onSurface = MeduzaTextWhite
        )
    ) {
        val platform = remember { getPlatform() }
        var selectedTab by remember { mutableStateOf(0) }

        val sampleAnime = remember {
            listOf(
                AnimeItem("1", "Solo Leveling: Arise", "12 qism", "9.8", "SERIAL", "2025"),
                AnimeItem("2", "Demon Slayer: Hashira", "8 qism", "9.6", "SERIAL", "2024"),
                AnimeItem("3", "Jujutsu Kaisen: Shibuya", "24 qism", "9.7", "SERIAL", "2024"),
                AnimeItem("4", "Attack on Titan: Final", "Film", "9.9", "FILM", "2024"),
                AnimeItem("5", "Chainsaw Man: Reze Arc", "Film", "9.5", "FILM", "2025")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MeduzaBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Meduza Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MeduzaCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MEDUZA",
                                color = MeduzaTextWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "ANIME • ${platform.name}",
                                color = MeduzaCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // VIP Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MeduzaGold.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "⭐ VIP",
                            color = MeduzaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Featured Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E2640), Color(0xFF0F1420))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MeduzaRed)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "YANGI PREMYERA",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Solo Leveling: Arise",
                                    color = MeduzaTextWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "O'zbek tilida • 1080p Full HD • Yangi qism yuklandi",
                                    color = MeduzaTextGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MeduzaCyan)
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "▶ Tomosha qilish",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Section: Ommabop Animelar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OMMABOP ANIMELAR",
                                color = MeduzaTextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Barchasi >",
                                color = MeduzaCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Anime List Items
                    items(sampleAnime, key = { it.id }) { anime ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MeduzaCard)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Poster placeholder
                            Box(
                                modifier = Modifier
                                    .size(width = 60.dp, height = 80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MeduzaCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = anime.title.take(1),
                                    color = MeduzaCyan,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (anime.type == "FILM") MeduzaCyan else MeduzaRed)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = anime.type,
                                            color = if (anime.type == "FILM") Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = anime.year,
                                        color = MeduzaTextGray,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = anime.title,
                                    color = MeduzaTextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "★ ${anime.rating}",
                                        color = MeduzaGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${anime.episodes}",
                                        color = MeduzaTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MeduzaCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "▶",
                                    color = MeduzaCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Bottom Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MeduzaBackground)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems = listOf("Asosiy", "Katalog", "Qidiruv", "Sevimlilar", "Profil")
                    val navIcons = listOf("🏠", "🧭", "🔍", "❤️", "👤")

                    navItems.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = navIcons[index],
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MeduzaCyan else MeduzaTextMuted
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MeduzaCyan)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
