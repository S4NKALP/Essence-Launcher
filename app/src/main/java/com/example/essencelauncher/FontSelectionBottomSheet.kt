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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.FileOutputStream

/**
 * BottomSheetDialog that:
 * - Disables swipe-to-dismiss
 * - Keeps tap outside, back button, and programmatic `.hide()` working
 */
class FontSelectionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var fontsContainer: LinearLayout
    private lateinit var currentFont: FontManager.FontInfo
    
    private val fontPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importCustomFont(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_font_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fontsContainer = view.findViewById(R.id.fontsContainer)
        currentFont = FontManager.getCurrentFont(requireContext())

        setupBottomSheetBehavior()
        setupFontList()
        applyFonts()
    }

    private fun setupBottomSheetBehavior() {
        val dialog = dialog as? BottomSheetDialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.isDraggable = false
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun setupFontList() {
        fontsContainer.removeAllViews()

        val availableFonts = FontManager.getAllAvailableFonts(requireContext())

        // Add system fonts first
        val systemFonts = availableFonts.filter { it.type == FontManager.FontType.SYSTEM }
        for (fontInfo in systemFonts) {
            addFontItem(fontInfo)
        }

        // Add custom font button after system fonts
        setupAddCustomFontButton()

        // Add resource fonts
        val resourceFonts = availableFonts.filter { it.type == FontManager.FontType.RESOURCE }
        for (fontInfo in resourceFonts) {
            addFontItem(fontInfo)
        }

        // Add custom fonts
        val customFonts = availableFonts.filter { it.type == FontManager.FontType.CUSTOM }
        for (fontInfo in customFonts) {
            addFontItem(fontInfo)
        }
    }

    private fun addFontItem(fontInfo: FontManager.FontInfo) {
        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_font_selection, fontsContainer, false)

        val fontNameText = itemView.findViewById<TextView>(R.id.fontName)
        val fontPreviewText = itemView.findViewById<TextView>(R.id.fontPreview)
        val fontTypeText = itemView.findViewById<TextView>(R.id.fontType)
        val selectedIndicator = itemView.findViewById<View>(R.id.selectedIndicator)

        fontNameText.text = fontInfo.name

        // Hide the font type label and preview text
        fontPreviewText.visibility = View.GONE
        fontTypeText.visibility = View.GONE

        // Show selection indicator if this is the current font
        selectedIndicator.visibility = if (fontInfo.name == currentFont.name && fontInfo.type == currentFont.type) {
            View.VISIBLE
        } else {
            View.GONE
        }

        itemView.setOnClickListener {
            selectFont(fontInfo)
        }

        fontsContainer.addView(itemView)
    }

    private fun setupAddCustomFontButton() {
        val addCustomFontView = LayoutInflater.from(requireContext()).inflate(R.layout.item_add_custom_font, fontsContainer, false)
        
        addCustomFontView.setOnClickListener {
            openFontPicker()
        }
        
        fontsContainer.addView(addCustomFontView)
    }

    private fun openFontPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf"))
        }
        
        try {
            fontPickerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open file picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importCustomFont(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Cannot read font file", Toast.LENGTH_SHORT).show()
                return
            }

            // Get the original filename
            val fileName = getFileName(uri) ?: "custom_font.ttf"
            
            // Ensure it has a valid font extension
            val validFileName = if (fileName.endsWith(".ttf", true) || fileName.endsWith(".otf", true)) {
                fileName
            } else {
                "$fileName.ttf"
            }

            // Create fonts directory and copy file
            val fontsDir = FontManager.createFontsDirectory(requireContext())
            val fontFile = File(fontsDir, validFileName)

            // Copy the font file
            FileOutputStream(fontFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            Toast.makeText(requireContext(), "Font imported successfully", Toast.LENGTH_SHORT).show()

            // Refresh the font list
            setupFontList()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to import font: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String? {
        return try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun selectFont(fontInfo: FontManager.FontInfo) {
        FontManager.setCurrentFont(requireContext(), fontInfo)
        Toast.makeText(requireContext(), "Font changed to ${fontInfo.name}", Toast.LENGTH_SHORT).show()
        
        // Dismiss the bottom sheet
        dismiss()
        
        // Restart the launcher to apply the new font
        restartLauncher()
    }

    private fun restartLauncher() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun applyFonts() {
        // Apply fonts to the entire fonts container
        FontUtils.applyFontToViewGroup(requireContext(), fontsContainer)
        
        // Apply font to the header
        val headerText = view?.findViewById<TextView>(R.id.headerTitle)
        headerText?.let { FontUtils.applyFontToTextView(requireContext(), it) }
    }

    companion object {
        fun newInstance(): FontSelectionBottomSheet {
            return FontSelectionBottomSheet()
        }
    }
}
