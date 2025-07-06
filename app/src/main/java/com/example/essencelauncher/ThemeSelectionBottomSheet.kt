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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheetDialog for theme mode selection with hot reload functionality.
 * Features:
 * - Theme mode selection (System, Light, Dark)
 * - Hot reload without app restart
 * - Locked behavior (no swipe-to-dismiss)
 * - Material Design 3 styling
 */
class ThemeSelectionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var themesContainer: LinearLayout
    private lateinit var cancelButton: Button
    private lateinit var applyButton: Button
    private var currentTheme: ThemeManager.ThemeMode = ThemeManager.ThemeMode.SYSTEM
    private var selectedTheme: ThemeManager.ThemeMode = ThemeManager.ThemeMode.SYSTEM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_theme_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupBottomSheetBehavior()
        loadCurrentTheme()
        setupThemeList()
        setupButtonListeners()
        applyFonts()
    }

    private fun initializeViews(view: View) {
        themesContainer = view.findViewById(R.id.themesContainer)
        cancelButton = view.findViewById(R.id.cancelButton)
        applyButton = view.findViewById(R.id.applyButton)
    }

    private fun loadCurrentTheme() {
        currentTheme = ThemeManager.getCurrentThemeMode(requireContext())
        selectedTheme = currentTheme
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

    private fun setupThemeList() {
        themesContainer.removeAllViews()

        val availableThemes = ThemeManager.getAllThemeModes()

        for (themeMode in availableThemes) {
            addThemeItem(themeMode)
        }
    }

    private fun addThemeItem(themeMode: ThemeManager.ThemeMode) {
        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_theme_selection, themesContainer, false)

        val themeNameText = itemView.findViewById<TextView>(R.id.themeName)
        val themeDescriptionText = itemView.findViewById<TextView>(R.id.themeDescription)
        val selectedIndicator = itemView.findViewById<View>(R.id.selectedIndicator)

        themeNameText.text = themeMode.displayName

        // Set description based on theme mode
        themeDescriptionText.text = when (themeMode) {
            ThemeManager.ThemeMode.SYSTEM -> "Follow system theme setting"
            ThemeManager.ThemeMode.LIGHT -> "Always use light theme"
            ThemeManager.ThemeMode.DARK -> "Always use dark theme"
        }

        // Show selection indicator if this is the selected theme
        selectedIndicator.visibility = if (themeMode == selectedTheme) {
            View.VISIBLE
        } else {
            View.GONE
        }

        itemView.setOnClickListener {
            updateSelection(themeMode)
        }

        themesContainer.addView(itemView)
    }

    private fun setupButtonListeners() {
        cancelButton.setOnClickListener {
            cancelSelection()
        }

        applyButton.setOnClickListener {
            applyTheme()
        }
    }

    private fun updateSelection(themeMode: ThemeManager.ThemeMode) {
        selectedTheme = themeMode
        // Refresh the theme list to update selection indicators
        setupThemeList()
    }

    private fun applyTheme() {
        if (selectedTheme != currentTheme) {
            ThemeManager.setThemeMode(requireContext(), selectedTheme)
            Toast.makeText(requireContext(), "Theme changed to ${selectedTheme.displayName}", Toast.LENGTH_SHORT).show()

            // Restart the launcher to apply the new theme
            restartLauncher()
        }
        dismiss()
    }

    private fun cancelSelection() {
        // Reset to current theme if user cancels
        selectedTheme = currentTheme
        dismiss()
    }

    private fun restartLauncher() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun applyFonts() {
        // Apply fonts to the entire themes container
        FontUtils.applyFontToViewGroup(requireContext(), themesContainer)

        // Apply font to the header
        val headerText = view?.findViewById<TextView>(R.id.headerTitle)
        headerText?.let { FontUtils.applyFontToTextView(requireContext(), it) }

        // Apply dynamic text colors
        TextColorManager.applyTextColorsToViewGroup(requireContext(), themesContainer)
        headerText?.let { TextColorManager.applyTextColor(requireContext(), it) }

        // Apply text colors to buttons
        TextColorManager.applyTextColor(requireContext(), cancelButton)
        TextColorManager.applyTextColor(requireContext(), applyButton)
    }

    companion object {
        fun newInstance(): ThemeSelectionBottomSheet {
            return ThemeSelectionBottomSheet()
        }
    }
}
