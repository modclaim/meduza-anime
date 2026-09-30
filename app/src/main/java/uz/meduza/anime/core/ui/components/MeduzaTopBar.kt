package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import uz.meduza.anime.R
import uz.meduza.anime.core.ui.theme.BackgroundDark
import uz.meduza.anime.core.ui.theme.CardDarkElevated
import uz.meduza.anime.core.ui.theme.PrimaryCyan
import uz.meduza.anime.core.ui.theme.TextWhite

@Composable
fun MeduzaTopBar(
    avatarUrl: String? = null,
    onAvatarClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Profile Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardDarkElevated)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = PrimaryCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Center Mascot Logo
        Image(
            painter = painterResource(id = R.drawable.ic_meduza_logo),
            contentDescription = "Meduza Logo",
            modifier = Modifier.size(36.dp)
        )

        // Right Download / Notification icon
        IconButton(
            onClick = onDownloadClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardDarkElevated)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Downloads",
                tint = TextWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
