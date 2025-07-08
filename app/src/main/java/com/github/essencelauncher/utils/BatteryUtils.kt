package com.github.essencelauncher.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility class for battery-related operations
 */
object BatteryUtils {

    /**
     * Data class to hold battery information
     */
    data class BatteryInfo(
        val level: Int,
        val isCharging: Boolean,
        val isLowBattery: Boolean,
        val icon: ImageVector
    )

    /**
     * Gets current battery information
     */
    fun getBatteryInfo(context: Context): BatteryInfo {
        val batteryStatus = getBatteryStatus(context)
        val level = getBatteryLevel(batteryStatus)
        val isCharging = isCharging(batteryStatus)
        val isLowBattery = level <= 20
        val icon = getBatteryIcon(level, isCharging)

        return BatteryInfo(
            level = level,
            isCharging = isCharging,
            isLowBattery = isLowBattery,
            icon = icon
        )
    }

    /**
     * Gets the battery status intent
     */
    private fun getBatteryStatus(context: Context): Intent? {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return context.registerReceiver(null, intentFilter)
    }

    /**
     * Gets the current battery level as a percentage
     */
    private fun getBatteryLevel(batteryStatus: Intent?): Int {
        if (batteryStatus == null) return 0

        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }
    }

    /**
     * Checks if the battery is currently charging
     */
    private fun isCharging(batteryStatus: Intent?): Boolean {
        if (batteryStatus == null) return false

        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * Gets the appropriate battery icon based on level and charging state
     */
    private fun getBatteryIcon(level: Int, isCharging: Boolean): ImageVector {
        return when {
            isCharging -> Icons.Default.BatteryChargingFull
            level <= 20 -> Icons.Default.BatteryAlert
            level <= 30 -> Icons.Default.Battery2Bar
            level <= 50 -> Icons.Default.Battery3Bar
            level <= 60 -> Icons.Default.Battery4Bar
            level <= 80 -> Icons.Default.Battery5Bar
            level <= 90 -> Icons.Default.Battery6Bar

            else -> Icons.Default.BatteryFull
        }
    }

    /**
     * Formats battery percentage for display
     */
    fun formatBatteryPercentage(level: Int): String {
        return "$level%"
    }
}
