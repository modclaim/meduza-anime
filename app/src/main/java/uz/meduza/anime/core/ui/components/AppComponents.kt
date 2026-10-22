package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.meduza.anime.core.ui.theme.*

@Composable
fun ScreenHeading(
    title: String,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = TextWhite)
            if (subtitle != null)
                Text(
                    subtitle,
                    color = TextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
        }
        action?.invoke()
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Info,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = CardDark) {
            Icon(icon, null, tint = PrimaryCyan, modifier = Modifier.padding(20.dp).size(30.dp))
        }
        Text(
            title,
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            message,
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        if (actionLabel != null)
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(actionLabel)
            }
    }
}

@Composable
fun InlineMessage(message: String, retry: (() -> Unit)? = null) {
    Surface(
        color = CardDark,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = TextGray, fontSize = 13.sp)
            if (retry != null) TextButton(onClick = retry) { Text("Qayta urinish") }
        }
    }
}
