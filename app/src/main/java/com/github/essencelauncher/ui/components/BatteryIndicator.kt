package com.github.essencelauncher.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.essencelauncher.utils.BatteryUtils
import com.github.essencelauncher.utils.managers.ItemAlignmentManager
import kotlinx.coroutines.delay

/**
 * Battery indicator composable that shows battery icon with percentage
 */
@Composable
fun BatteryIndicator(
    itemAlignmentManager: ItemAlignmentManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf(BatteryUtils.getBatteryInfo(context)) }

    // Update battery info more frequently
    LaunchedEffect(Unit) {
        while (true) {
            batteryInfo = BatteryUtils.getBatteryInfo(context)
            delay(5000) // Update every 5 seconds for more responsive updates
        }
    }

    val batteryAlignment = itemAlignmentManager.getBatteryAlignmentAsHorizontal()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = when (batteryAlignment) {
            Alignment.Start -> Alignment.Start
            Alignment.End -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Battery icon
            Icon(
                imageVector = batteryInfo.icon,
                contentDescription = "Battery",
                tint = when {
                    batteryInfo.isCharging -> MaterialTheme.colorScheme.primary
                    batteryInfo.isLowBattery -> Color.Red
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.size(20.dp)
            )

            // Battery percentage
            Text(
                text = BatteryUtils.formatBatteryPercentage(batteryInfo.level),
                color = when {
                    batteryInfo.isCharging -> MaterialTheme.colorScheme.primary
                    batteryInfo.isLowBattery -> Color.Red
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = when (batteryAlignment) {
                    Alignment.Start -> TextAlign.Start
                    Alignment.End -> TextAlign.End
                    else -> TextAlign.Center
                }
            )
        }
    }
}
