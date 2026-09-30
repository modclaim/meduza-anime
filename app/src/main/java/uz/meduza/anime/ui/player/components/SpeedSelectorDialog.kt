package uz.meduza.anime.ui.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.meduza.anime.core.ui.theme.CardDark
import uz.meduza.anime.core.ui.theme.PrimaryCyan
import uz.meduza.anime.core.ui.theme.TextGray
import uz.meduza.anime.core.ui.theme.TextWhite

@Composable
fun SpeedSelectorDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text("Ijro Tezligi (Speed)", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                speeds.forEach { speed ->
                    val isSelected = currentSpeed == speed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSpeedSelected(speed)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (speed == 1.0f) "1.0x (Oddiy)" else "${speed}x",
                            color = if (isSelected) PrimaryCyan else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (isSelected) {
                            Text("✓", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ResizeModeDialog(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val modes = listOf(
        "FIT" to "Moslash (Fit to screen)",
        "ZOOM" to "Kattalashtirish (Crop/Zoom)",
        "FILL" to "To'liq ekran (Fill)",
        "STRETCH" to "Cho'zish (Stretch)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text("Ekran Formati (Aspect Ratio)", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                modes.forEach { (modeKey, modeTitle) ->
                    val isSelected = currentMode == modeKey
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModeSelected(modeKey)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = modeTitle,
                            color = if (isSelected) PrimaryCyan else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (isSelected) {
                            Text("✓", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
