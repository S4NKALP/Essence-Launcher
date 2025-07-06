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

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    
    private const val PREF_THEME_MODE = "theme_mode"
    
    enum class ThemeMode(val displayName: String, val value: Int) {
        SYSTEM("System", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT("Light", AppCompatDelegate.MODE_NIGHT_NO),
        DARK("Dark", AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    /**
     * Get the current theme mode
     */
    fun getCurrentThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val savedMode = prefs.getString(PREF_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(savedMode ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * Set the theme mode
     */
    fun setThemeMode(context: Context, themeMode: ThemeMode) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_THEME_MODE, themeMode.name).apply()
        
        // Apply the theme immediately
        AppCompatDelegate.setDefaultNightMode(themeMode.value)
    }
    
    /**
     * Apply the saved theme mode
     */
    fun applySavedThemeMode(context: Context) {
        val currentTheme = getCurrentThemeMode(context)
        AppCompatDelegate.setDefaultNightMode(currentTheme.value)
    }
    
    /**
     * Get all available theme modes
     */
    fun getAllThemeModes(): List<ThemeMode> {
        return ThemeMode.values().toList()
    }
    
    /**
     * Check if the current system is in dark mode
     */
    fun isSystemInDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * Get the effective theme mode (resolves SYSTEM to actual LIGHT/DARK)
     */
    fun getEffectiveThemeMode(context: Context): ThemeMode {
        val currentMode = getCurrentThemeMode(context)
        return if (currentMode == ThemeMode.SYSTEM) {
            if (isSystemInDarkMode(context)) ThemeMode.DARK else ThemeMode.LIGHT
        } else {
            currentMode
        }
    }

    /**
     * Get the appropriate text color based on theme mode and wallpaper opacity
     */
    fun getTextColor(context: Context): Int {
        val effectiveTheme = getEffectiveThemeMode(context)
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val opacity = prefs.getInt("wallpaper_opacity", 20) // Default 20%

        return when (effectiveTheme) {
            ThemeMode.LIGHT -> {
                // In light mode, use black text for better contrast
                // Adjust opacity based on wallpaper transparency
                if (opacity > 50) {
                    // High transparency (showing more wallpaper) - use solid black
                    android.graphics.Color.BLACK
                } else {
                    // Low transparency (darker overlay) - use black but could be lighter
                    android.graphics.Color.BLACK
                }
            }
            ThemeMode.DARK -> {
                // In dark mode, use white text
                if (opacity > 50) {
                    // High transparency (showing more wallpaper) - use solid white
                    android.graphics.Color.WHITE
                } else {
                    // Low transparency (darker overlay) - use white
                    android.graphics.Color.WHITE
                }
            }
            ThemeMode.SYSTEM -> {
                // This should not happen as getEffectiveThemeMode resolves SYSTEM
                if (isSystemInDarkMode(context)) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            }
        }
    }

    /**
     * Get the appropriate secondary text color based on theme mode and wallpaper opacity
     */
    fun getSecondaryTextColor(context: Context): Int {
        val effectiveTheme = getEffectiveThemeMode(context)
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val opacity = prefs.getInt("wallpaper_opacity", 20) // Default 20%

        return when (effectiveTheme) {
            ThemeMode.LIGHT -> {
                // In light mode, use slightly lighter black for secondary text
                if (opacity > 50) {
                    android.graphics.Color.parseColor("#FF666666") // Dark gray
                } else {
                    android.graphics.Color.parseColor("#FF333333") // Darker gray
                }
            }
            ThemeMode.DARK -> {
                // In dark mode, use slightly darker white for secondary text
                if (opacity > 50) {
                    android.graphics.Color.parseColor("#FFCCCCCC") // Light gray
                } else {
                    android.graphics.Color.parseColor("#FFEEEEEE") // Lighter gray
                }
            }
            ThemeMode.SYSTEM -> {
                // This should not happen as getEffectiveThemeMode resolves SYSTEM
                if (isSystemInDarkMode(context)) android.graphics.Color.parseColor("#FFCCCCCC") else android.graphics.Color.parseColor("#FF666666")
            }
        }
    }
}
