package uz.meduza.anime.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.components.PrimaryButton
import uz.meduza.anime.core.ui.theme.*

@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var malQuery by remember { mutableStateOf("") }
    var premiumUserId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Admin Boshqaruv Paneli", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Stats Cards
            item {
                val stats = uiState.stats
                Text(text = "PLATFORMA STATISTIKASI", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Foydalanuvchilar", color = TextGray, fontSize = 11.sp)
                            Text("${stats?.totalUsers ?: 0}", color = PrimaryCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("VIP Premium", color = TextGray, fontSize = 11.sp)
                            Text("${stats?.premiumUsers ?: 0}", color = PremiumGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Animelar", color = TextGray, fontSize = 11.sp)
                            Text("${stats?.totalAnimes ?: 0}", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // MyAnimeList Quick Fetch & Import
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "MyAnimeList (MAL) Integratsiyasi", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Anime ma'lumotlarini rasmiy MAL bazasidan avtomatik import qilish", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = malQuery,
                                onValueChange = { malQuery = it },
                                placeholder = { Text("MAL anime nomi...", color = TextGray, fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InputBackground,
                                    unfocusedContainerColor = InputBackground,
                                    focusedBorderColor = PrimaryCyan,
                                    unfocusedBorderColor = InputBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.searchMal(malQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = OnPrimary),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Qidirish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // MAL Results List
            if (uiState.malResults.isNotEmpty()) {
                items(uiState.malResults) { malItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardDarkElevated)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = malItem.posterUrl,
                            contentDescription = malItem.title,
                            modifier = Modifier
                                .size(50.dp, 70.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = malItem.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "MAL ID: ${malItem.malId} • Qismlar: ${malItem.episodes ?: "?"}", color = TextGray, fontSize = 11.sp)
                            Text(text = "MAL Baho: ★ ${malItem.score ?: "N/A"}", color = StarGold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Grant VIP Premium to User
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "VIP Premium Berish", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = premiumUserId,
                            onValueChange = { premiumUserId = it },
                            placeholder = { Text("User ID (UUID)", color = TextGray, fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputBackground,
                                unfocusedContainerColor = InputBackground,
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = InputBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PrimaryButton(
                            text = "30 Kunlik VIP Premium Taqdim Etish",
                            containerColor = PremiumGold,
                            contentColor = Color.Black,
                            onClick = {
                                viewModel.grantPremium(premiumUserId, 30)
                                premiumUserId = ""
                            }
                        )
                    }
                }
            }
        }
    }
}
