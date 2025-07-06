/*
 * Copyright (C) 2025 Sankalp Tharu
 *
 * This file is part of EssenceLauncher.
 *
 * EssenceLauncher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.essencelauncher

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsContainer = findViewById(R.id.settingsContainer)

        // Apply wallpaper background immediately
        applyWallpaperBackground()

        setupSettingsItems()
        applyFonts()
    }

    override fun onResume() {
        super.onResume()
        // Refresh wallpaper background when returning to settings
        applyWallpaperBackground()
    }

    private fun setupSettingsItems() {
        // Time format toggle
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val is24HourFormat = prefs.getBoolean("time_24_hour_format", false)

        addToggleSettingsItem("24-Hour Format", "Use 24-hour time format (14:30) instead of 12-hour (2:30 PM)", is24HourFormat) { isEnabled ->
            prefs.edit().putBoolean("time_24_hour_format", isEnabled).apply()
            val formatName = if (isEnabled) "24-hour format" else "12-hour format"
            Toast.makeText(this, "Time format changed to $formatName", Toast.LENGTH_SHORT).show()
        }

        // Font family
        val currentFont = FontManager.getCurrentFont(this)
        addSettingsItem("Font Family", "Current: ${currentFont.name}") {
            openFontSelection()
        }

        // Show wallpaper toggle
        val showWallpaper = WallpaperManager.isShowWallpaperEnabled(this)
        addToggleSettingsItem("Show Wallpaper", "Display system wallpaper behind launcher", showWallpaper) { isEnabled ->
            WallpaperManager.setShowWallpaperEnabled(this, isEnabled)
            // Refresh wallpaper immediately
            applyWallpaperBackground()
            // Refresh settings to show/hide opacity option
            refreshSettingsDisplay()
            val statusText = if (isEnabled) "enabled" else "disabled"
            Toast.makeText(this, "Wallpaper display $statusText", Toast.LENGTH_SHORT).show()
        }

        // Wallpaper opacity (only show if wallpaper is disabled)
        if (!showWallpaper) {
            val currentOpacity = WallpaperManager.getWallpaperOpacity(this)
            addSettingsItem("Wallpaper Opacity", "Current: ${currentOpacity}%") {
                showWallpaperOpacityDialog()
            }
        }

        // Clear favorites
        addSettingsItem("Clear Favorites", "Remove all favorite apps") {
            showConfirmationDialog("Clear Favorites", "Are you sure you want to remove all favorite apps?") {
                clearFavorites()
            }
        }

        // Clear hidden apps
        addSettingsItem("Unhide All Apps", "Make all hidden apps visible") {
            showConfirmationDialog("Unhide All Apps", "Are you sure you want to make all hidden apps visible?") {
                clearHiddenApps()
            }
        }

        // Clear custom app names
        addSettingsItem("Reset App Names", "Reset all custom app names to default") {
            showConfirmationDialog("Reset App Names", "Are you sure you want to reset all custom app names?") {
                clearCustomNames()
            }
        }

        // Clear locked apps
        addSettingsItem("Unlock All Apps", "Remove locks from all apps") {
            showConfirmationDialog("Unlock All Apps", "Are you sure you want to unlock all apps?") {
                clearLockedApps()
            }
        }

        // Default launcher settings
        addSettingsItem("Default Launcher", "Set as default launcher") {
            openDefaultLauncherSettings()
        }

        // App info
        addSettingsItem("App Info", "View launcher app information") {
            openAppInfo()
        }

        // About
        addSettingsItem("About", "About Essence Launcher") {
            showAboutDialog()
        }
    }

    private fun addSettingsItem(title: String, description: String, onClick: () -> Unit) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_settings, settingsContainer, false)

        val titleText = itemView.findViewById<TextView>(R.id.settingTitle)
        val descriptionText = itemView.findViewById<TextView>(R.id.settingDescription)

        titleText.text = title
        descriptionText.text = description

        itemView.setOnClickListener { onClick() }

        settingsContainer.addView(itemView)
    }

    private fun addToggleSettingsItem(title: String, description: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_settings_toggle, settingsContainer, false)

        val titleText = itemView.findViewById<TextView>(R.id.settingTitle)
        val descriptionText = itemView.findViewById<TextView>(R.id.settingDescription)
        val switch = itemView.findViewById<Switch>(R.id.settingSwitch)

        titleText.text = title
        descriptionText.text = description
        switch.isChecked = isEnabled

        itemView.setOnClickListener {
            switch.isChecked = !switch.isChecked
            onToggle(switch.isChecked)
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            onToggle(isChecked)
        }

        settingsContainer.addView(itemView)
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun clearFavorites() {
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("favorite_apps").apply()
        Toast.makeText(this, "Favorites cleared", Toast.LENGTH_SHORT).show()
    }

    private fun clearHiddenApps() {
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("hidden_apps").apply()
        Toast.makeText(this, "All apps are now visible", Toast.LENGTH_SHORT).show()
    }

    private fun clearCustomNames() {
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("custom_app_names").apply()
        Toast.makeText(this, "App names reset to default", Toast.LENGTH_SHORT).show()
    }

    private fun clearLockedApps() {
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("locked_apps").apply()
        Toast.makeText(this, "All apps unlocked", Toast.LENGTH_SHORT).show()
    }

    private fun openFontSelection() {
        val fontBottomSheet = FontSelectionBottomSheet.newInstance()
        fontBottomSheet.show(supportFragmentManager, "FontSelectionBottomSheet")
    }

    private fun showWallpaperOpacityDialog() {
        val opacityBottomSheet = WallpaperOpacityBottomSheet.newInstance()
        opacityBottomSheet.show(supportFragmentManager, "WallpaperOpacityBottomSheet")
    }

    fun applyWallpaperBackground() {
        // Apply wallpaper background to the root container
        val rootContainer = findViewById<LinearLayout>(R.id.settingsRoot)
        if (rootContainer != null) {
            WallpaperManager.applyWallpaperBackground(this, rootContainer)
        } else {
            // Fallback: apply to the main content view
            val contentView = findViewById<View>(android.R.id.content)
            WallpaperManager.applyWallpaperBackground(this, contentView)
        }
    }

    fun refreshSettingsDisplay() {
        // Clear and rebuild the settings items to show updated values
        settingsContainer.removeAllViews()
        setupSettingsItems()
        applyFonts()
    }

    private fun applyFonts() {
        // Apply fonts to the entire settings container
        FontUtils.applyFontToViewGroup(this, settingsContainer)

        // Apply font to the header
        val headerText = findViewById<TextView>(R.id.headerTitle)
        headerText?.let { FontUtils.applyFontToTextView(this, it) }
    }

    private fun openDefaultLauncherSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open default launcher settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfo() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open app info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("About Essence Launcher")
            .setMessage("Essence Launcher\nVersion 1.0\n\nA minimalist Android launcher focused on simplicity and efficiency.\n\n© 2025 Sankalp Tharu")
            .setPositiveButton("OK", null)
            .show()
    }
}
