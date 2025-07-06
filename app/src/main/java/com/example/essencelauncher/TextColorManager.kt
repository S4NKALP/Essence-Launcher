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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object TextColorManager {

    /**
     * Apply dynamic text colors to a single TextView
     */
    fun applyTextColor(context: Context, textView: TextView, isSecondary: Boolean = false) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val wallpaperOpacity = prefs.getInt("wallpaper_opacity", 20)

        if (wallpaperOpacity >= 50) {
            // Use full white color for high wallpaper opacity
            textView.setTextColor(android.graphics.Color.WHITE)
        } else {
            // Use Material 3 primary color for low wallpaper opacity
            val typedArray = context.obtainStyledAttributes(intArrayOf(
                if (isSecondary) android.R.attr.textColorSecondary else android.R.attr.textColorPrimary
            ))
            val primaryColor = typedArray.getColor(0, android.graphics.Color.BLACK)
            typedArray.recycle()

            textView.setTextColor(primaryColor)
        }
    }
    
    /**
     * Apply dynamic text colors to all TextViews in a ViewGroup recursively
     */
    fun applyTextColorsToViewGroup(context: Context, viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child) {
                is TextView -> {
                    // Determine if this is secondary text based on text size or other criteria
                    val isSecondary = child.textSize < 16f || child.id == R.id.themeDescription
                    applyTextColor(context, child, isSecondary)
                }
                is ViewGroup -> applyTextColorsToViewGroup(context, child)
            }
        }
    }
    
    /**
     * Apply dynamic text colors to a View (if it's a TextView or ViewGroup)
     */
    fun applyTextColorsToView(context: Context, view: View) {
        when (view) {
            is TextView -> applyTextColor(context, view)
            is ViewGroup -> applyTextColorsToViewGroup(context, view)
        }
    }
    
    /**
     * Apply text colors to specific TextViews by their IDs
     */
    fun applyTextColorsToTextViews(context: Context, rootView: View, vararg textViewIds: Int) {
        textViewIds.forEach { id ->
            rootView.findViewById<TextView>(id)?.let { textView ->
                applyTextColor(context, textView)
            }
        }
    }
    
    /**
     * Apply text colors to specific TextViews with secondary color by their IDs
     */
    fun applySecondaryTextColorsToTextViews(context: Context, rootView: View, vararg textViewIds: Int) {
        textViewIds.forEach { id ->
            rootView.findViewById<TextView>(id)?.let { textView ->
                applyTextColor(context, textView, isSecondary = true)
            }
        }
    }
}
