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
import android.graphics.Color
import android.view.View

/**
 * Utility class for managing wallpaper display and background overlay functionality.
 * Provides methods to apply transparent backgrounds and custom color overlays.
 */
object WallpaperManager {

    private const val PREF_WALLPAPER_OPACITY = "wallpaper_opacity"
    private const val PREF_CUSTOM_BACKGROUND_ENABLED = "custom_background_enabled"
    private const val PREF_CUSTOM_BACKGROUND_COLOR = "custom_background_color"
    private const val PREF_SHOW_WALLPAPER = "show_wallpaper"
    private const val PREF_SAVED_OPACITY = "saved_opacity"

    // Default transparency is 20% as per user preference
    private const val DEFAULT_TRANSPARENCY = 20

    /**
     * Apply wallpaper background to a view with optional color overlay.
     * This makes the view transparent by default to show the system wallpaper,
     * or applies a custom background color with adjustable opacity.
     */
    fun applyWallpaperBackground(context: Context, view: View) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val isCustomBackgroundEnabled = prefs.getBoolean(PREF_CUSTOM_BACKGROUND_ENABLED, false)
        val opacity = getEffectiveWallpaperOpacity(context)

        if (isCustomBackgroundEnabled) {
            // Apply custom background color with user-defined opacity
            val backgroundColor = prefs.getInt(PREF_CUSTOM_BACKGROUND_COLOR, Color.BLACK)
            val overlayColor = getColorWithOpacity(backgroundColor, opacity)
            view.setBackgroundColor(overlayColor)
        } else {
            // Apply semi-transparent black overlay to show wallpaper dimming effect
            val overlayColor = getHexForOpacity(opacity)
            view.setBackgroundColor(Color.parseColor(overlayColor))
        }
    }

    /**
     * Get hex color string for black overlay with given transparency.
     * 0% = fully opaque black, 100% = fully transparent (no overlay)
     */
    fun getHexForOpacity(opacity: Int): String {
        // Convert transparency percentage to alpha value
        // 0% transparency = 255 alpha (fully opaque black)
        // 100% transparency = 0 alpha (fully transparent)
        val alpha = ((100 - opacity) * 255 / 100).coerceIn(0, 255)
        val alphaHex = String.format("%02X", alpha)
        return "#${alphaHex}000000" // Black overlay with calculated alpha
    }

    /**
     * Apply given transparency to a color and return result.
     * 0% = fully opaque, 100% = fully transparent.
     */
    private fun getColorWithOpacity(color: Int, opacity: Int): Int {
        // Convert transparency percentage to alpha value
        // 0% transparency = 255 alpha (fully opaque)
        // 100% transparency = 0 alpha (fully transparent)
        val alpha = ((100 - opacity) * 255 / 100).coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    /**
     * Get current wallpaper opacity setting.
     */
    fun getWallpaperOpacity(context: Context): Int {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        return prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
    }

    /**
     * Set wallpaper opacity setting (0–100).
     */
    fun setWallpaperOpacity(context: Context, opacity: Int) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_WALLPAPER_OPACITY, opacity.coerceIn(0, 100)).apply()
    }

    /**
     * Check if custom background is enabled.
     */
    fun isCustomBackgroundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_CUSTOM_BACKGROUND_ENABLED, false)
    }

    /**
     * Enable or disable custom background.
     */
    fun setCustomBackgroundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_CUSTOM_BACKGROUND_ENABLED, enabled).apply()
    }

    /**
     * Get custom background color.
     */
    fun getCustomBackgroundColor(context: Context): Int {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        return prefs.getInt(PREF_CUSTOM_BACKGROUND_COLOR, Color.BLACK)
    }

    /**
     * Set custom background color.
     */
    fun setCustomBackgroundColor(context: Context, color: Int) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_CUSTOM_BACKGROUND_COLOR, color).apply()
    }

    /**
     * Get the current wallpaper background color that should be applied.
     * This can be used to set default backgrounds in layouts to prevent flashing.
     */
    fun getCurrentWallpaperBackgroundColor(context: Context): Int {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val isCustomBackgroundEnabled = prefs.getBoolean(PREF_CUSTOM_BACKGROUND_ENABLED, false)

        return if (isCustomBackgroundEnabled) {
            // Apply custom background color with opacity
            val backgroundColor = prefs.getInt(PREF_CUSTOM_BACKGROUND_COLOR, Color.BLACK)
            val opacity = prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
            getColorWithOpacity(backgroundColor, opacity)
        } else {
            // Make background transparent to show wallpaper
            val opacity = prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
            Color.parseColor(getHexForOpacity(opacity))
        }
    }

    /**
     * Apply wallpaper background with a temporary opacity override (for preview).
     * This doesn't save the opacity to SharedPreferences.
     */
    fun applyWallpaperBackgroundWithOpacity(context: Context, view: View, previewOpacity: Int) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val isCustomBackgroundEnabled = prefs.getBoolean(PREF_CUSTOM_BACKGROUND_ENABLED, false)

        if (isCustomBackgroundEnabled) {
            // Apply custom background color with preview opacity
            val backgroundColor = prefs.getInt(PREF_CUSTOM_BACKGROUND_COLOR, Color.BLACK)
            val overlayColor = getColorWithOpacity(backgroundColor, previewOpacity)
            view.setBackgroundColor(overlayColor)
        } else {
            // Make background transparent to show wallpaper with preview opacity
            val overlayColor = getHexForOpacity(previewOpacity)
            view.setBackgroundColor(Color.parseColor(overlayColor))
        }
    }

    /**
     * Check if show wallpaper is enabled.
     */
    fun isShowWallpaperEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_SHOW_WALLPAPER, false) // Default to false (hide wallpaper)
    }

    /**
     * Set show wallpaper enabled/disabled.
     * When enabled: saves current opacity and sets to 100% (fully transparent to show wallpaper)
     * When disabled: restores saved opacity (to hide/dim wallpaper)
     */
    fun setShowWallpaperEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Initialize saved opacity if it doesn't exist
        initializeSavedOpacityIfNeeded(context)

        if (enabled) {
            // Save current opacity before enabling wallpaper display
            val currentOpacity = prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
            // Only save if it's not already 100% (to preserve user's actual preference)
            if (currentOpacity != 100) {
                editor.putInt(PREF_SAVED_OPACITY, currentOpacity)
            }
            // Set opacity to 100% (fully transparent to show wallpaper)
            editor.putInt(PREF_WALLPAPER_OPACITY, 100)
        } else {
            // Restore saved opacity when disabling wallpaper display
            val savedOpacity = prefs.getInt(PREF_SAVED_OPACITY, DEFAULT_TRANSPARENCY)
            editor.putInt(PREF_WALLPAPER_OPACITY, savedOpacity)
        }

        editor.putBoolean(PREF_SHOW_WALLPAPER, enabled)
        editor.apply()
    }

    /**
     * Get the effective wallpaper opacity (considering show wallpaper toggle).
     */
    fun getEffectiveWallpaperOpacity(context: Context): Int {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val showWallpaper = prefs.getBoolean(PREF_SHOW_WALLPAPER, false)

        // Always return the current opacity setting from preferences
        // The setShowWallpaperEnabled method handles setting the opacity to 100% when enabled
        return prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
    }

    /**
     * Initialize saved opacity if it doesn't exist.
     * This ensures we have a fallback when toggling wallpaper for the first time.
     */
    private fun initializeSavedOpacityIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains(PREF_SAVED_OPACITY)) {
            val currentOpacity = prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
            prefs.edit().putInt(PREF_SAVED_OPACITY, currentOpacity).apply()
        }
    }
}
