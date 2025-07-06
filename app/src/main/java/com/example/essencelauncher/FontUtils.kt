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
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object FontUtils {
    
    /**
     * Apply the current font to a single TextView
     */
    fun applyFontToTextView(context: Context, textView: TextView) {
        val typeface = FontManager.getTypeface(context)
        typeface?.let {
            textView.typeface = it
        }
    }
    
    /**
     * Apply the current font to all TextViews in a ViewGroup recursively
     */
    fun applyFontToViewGroup(context: Context, viewGroup: ViewGroup) {
        val typeface = FontManager.getTypeface(context)
        typeface?.let {
            applyTypefaceToViewGroup(viewGroup, it)
        }
    }
    
    /**
     * Apply the current font to all TextViews in a View (if it's a ViewGroup)
     */
    fun applyFontToView(context: Context, view: View) {
        val typeface = FontManager.getTypeface(context)
        typeface?.let {
            when (view) {
                is TextView -> view.typeface = it
                is ViewGroup -> applyTypefaceToViewGroup(view, it)
            }
        }
    }
    
    /**
     * Recursively apply typeface to all TextViews in a ViewGroup
     */
    private fun applyTypefaceToViewGroup(viewGroup: ViewGroup, typeface: Typeface) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child) {
                is TextView -> child.typeface = typeface
                is ViewGroup -> applyTypefaceToViewGroup(child, typeface)
            }
        }
    }
    
    /**
     * Apply font to specific TextViews by their IDs
     */
    fun applyFontToTextViews(context: Context, rootView: View, vararg textViewIds: Int) {
        val typeface = FontManager.getTypeface(context)
        typeface?.let {
            textViewIds.forEach { id ->
                rootView.findViewById<TextView>(id)?.typeface = it
            }
        }
    }
}
