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
import androidx.core.content.res.ResourcesCompat
import java.io.File

object FontManager {
    
    data class FontInfo(
        val name: String,
        val type: FontType,
        val resourceId: Int? = null,
        val filePath: String? = null
    )
    
    enum class FontType {
        SYSTEM,
        RESOURCE,
        CUSTOM
    }
    
    private const val PREF_SELECTED_FONT = "selected_font"
    private const val PREF_FONT_TYPE = "font_type"
    private const val PREF_FONT_RESOURCE_ID = "font_resource_id"
    private const val PREF_FONT_FILE_PATH = "font_file_path"
    
    // Available system fonts (using Android system font)
    private val systemFonts = listOf(
        FontInfo("System", FontType.SYSTEM)
    )
    
    // Available resource fonts
    private val resourceFonts = listOf<FontInfo>(
        FontInfo("Bitter", FontType.RESOURCE, R.font.bitter),
        FontInfo("Doto", FontType.RESOURCE, R.font.doto),
        FontInfo("Droid Sans", FontType.RESOURCE, R.font.droid_sans),
        FontInfo("Fira Code", FontType.RESOURCE, R.font.fira_code),
        FontInfo("Hack", FontType.RESOURCE, R.font.hack),
        FontInfo("Lato", FontType.RESOURCE, R.font.lato),
        FontInfo("Merriweather", FontType.RESOURCE, R.font.merriweather),
        FontInfo("Montserrat", FontType.RESOURCE, R.font.montserrat),
        FontInfo("Noto Sans", FontType.RESOURCE, R.font.noto_sans),
        FontInfo("Open Sans", FontType.RESOURCE, R.font.open_sans),
        FontInfo("Quicksand", FontType.RESOURCE, R.font.quicksand),
        FontInfo("Raleway", FontType.RESOURCE, R.font.raleway),
        FontInfo("Roboto", FontType.RESOURCE, R.font.roboto),
        FontInfo("Source Code Pro", FontType.RESOURCE, R.font.source_code_pro)
    )
    
    fun getAllAvailableFonts(context: Context): List<FontInfo> {
        val allFonts = mutableListOf<FontInfo>()
        
        // Add system fonts
        allFonts.addAll(systemFonts)
        
        // Add resource fonts
        allFonts.addAll(resourceFonts)
        
        // Add custom fonts from external storage
        allFonts.addAll(getCustomFonts(context))
        
        return allFonts
    }
    
    private fun getCustomFonts(context: Context): List<FontInfo> {
        val customFonts = mutableListOf<FontInfo>()
        
        try {
            // Check for fonts in app's external files directory
            val fontsDir = File(context.getExternalFilesDir(null), "fonts")
            if (fontsDir.exists() && fontsDir.isDirectory) {
                fontsDir.listFiles { file ->
                    file.isFile && (file.extension.lowercase() == "ttf" || file.extension.lowercase() == "otf")
                }?.forEach { fontFile ->
                    val fontName = fontFile.nameWithoutExtension
                    customFonts.add(FontInfo(fontName, FontType.CUSTOM, filePath = fontFile.absolutePath))
                }
            }
        } catch (e: Exception) {
            // Handle any file system errors
        }
        
        return customFonts
    }
    
    fun getCurrentFont(context: Context): FontInfo {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val fontName = prefs.getString(PREF_SELECTED_FONT, "System") ?: "System"
        val fontType = FontType.valueOf(prefs.getString(PREF_FONT_TYPE, FontType.SYSTEM.name) ?: FontType.SYSTEM.name)
        val resourceId = prefs.getInt(PREF_FONT_RESOURCE_ID, -1).takeIf { it != -1 }
        val filePath = prefs.getString(PREF_FONT_FILE_PATH, null)

        return FontInfo(fontName, fontType, resourceId, filePath)
    }
    
    fun setCurrentFont(context: Context, fontInfo: FontInfo) {
        val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(PREF_SELECTED_FONT, fontInfo.name)
            putString(PREF_FONT_TYPE, fontInfo.type.name)
            fontInfo.resourceId?.let { putInt(PREF_FONT_RESOURCE_ID, it) } ?: remove(PREF_FONT_RESOURCE_ID)
            fontInfo.filePath?.let { putString(PREF_FONT_FILE_PATH, it) } ?: remove(PREF_FONT_FILE_PATH)
            apply()
        }
    }
    
    fun getTypeface(context: Context, fontInfo: FontInfo? = null): Typeface? {
        val font = fontInfo ?: getCurrentFont(context)
        
        return when (font.type) {
            FontType.SYSTEM -> {
                when (font.name) {
                    "System" -> Typeface.DEFAULT
                    else -> Typeface.DEFAULT
                }
            }
            FontType.RESOURCE -> {
                font.resourceId?.let { resourceId ->
                    try {
                        ResourcesCompat.getFont(context, resourceId)
                    } catch (e: Exception) {
                        Typeface.DEFAULT
                    }
                } ?: Typeface.DEFAULT
            }
            FontType.CUSTOM -> {
                font.filePath?.let { filePath ->
                    try {
                        if (File(filePath).exists()) {
                            Typeface.createFromFile(filePath)
                        } else {
                            Typeface.DEFAULT
                        }
                    } catch (e: Exception) {
                        Typeface.DEFAULT
                    }
                } ?: Typeface.DEFAULT
            }
        }
    }
    
    fun applyFontToContext(context: Context) {
        // This method can be used to apply font globally if needed
        // For now, we'll apply fonts individually to views
    }
    
    fun createFontsDirectory(context: Context): File {
        val fontsDir = File(context.getExternalFilesDir(null), "fonts")
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }
        return fontsDir
    }
}
