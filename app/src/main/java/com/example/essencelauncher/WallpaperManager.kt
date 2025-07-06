/*
 * Copyright (C) 2025 Sankalp Tharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        
        if (isCustomBackgroundEnabled) {
            // Apply custom background color with opacity
            val backgroundColor = prefs.getInt(PREF_CUSTOM_BACKGROUND_COLOR, Color.BLACK)
            val opacity = prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
            val overlayColor = getColorWithOpacity(backgroundColor, opacity)
            view.setBackgroundColor(overlayColor)
        } else {
            // Make background transparent to show wallpaper
            val opacity = prefs.getInt(PREF_WALLPAPER_OPACITY, DEFAULT_TRANSPARENCY)
            val overlayColor = getHexForOpacity(opacity)
            view.setBackgroundColor(Color.parseColor(overlayColor))
        }
    }
    
    /**
     * Get hex color string for opacity overlay.
     * Uses reversed opacity calculation: 0% = full opacity, 100% = transparent
     * as per user preference.
     */
    fun getHexForOpacity(opacity: Int): String {
        // Reverse the opacity: 0% input = 100% actual opacity, 100% input = 0% actual opacity
        val reversedOpacity = 100 - opacity
        val alpha = (reversedOpacity * 255 / 100).coerceIn(0, 255)
        val alphaHex = String.format("%02X", alpha)
        return "#${alphaHex}000000" // Black overlay with calculated alpha
    }
    
    /**
     * Apply opacity to a color.
     */
    private fun getColorWithOpacity(color: Int, opacity: Int): Int {
        // Reverse the opacity: 0% input = 100% actual opacity, 100% input = 0% actual opacity
        val reversedOpacity = 100 - opacity
        val alpha = (reversedOpacity * 255 / 100).coerceIn(0, 255)
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
     * Set wallpaper opacity setting.
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
}
