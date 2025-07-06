/*
 * Copyright (C) 2024 Essence Launcher
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheetDialog for wallpaper opacity selection with hot reload functionality.
 * Features:
 * - Real-time opacity preview
 * - Hot reload without app restart
 * - Locked behavior (no swipe-to-dismiss)
 * - Material Design 3 styling
 */
class WallpaperOpacityBottomSheet : BottomSheetDialogFragment() {

    private lateinit var opacitySeekBar: SeekBar
    private lateinit var opacityValueText: TextView
    private lateinit var cancelButton: Button
    private lateinit var applyButton: Button
    
    private var currentOpacity: Int = 20
    private var initialOpacity: Int = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_wallpaper_opacity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupBottomSheetBehavior()
        loadCurrentOpacity()
        setupSeekBarListener()
        setupButtonListeners()
        applyFonts()
    }

    private fun initializeViews(view: View) {
        opacitySeekBar = view.findViewById(R.id.opacitySeekBar)
        opacityValueText = view.findViewById(R.id.opacityValueText)
        cancelButton = view.findViewById(R.id.cancelButton)
        applyButton = view.findViewById(R.id.applyButton)
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

    private fun loadCurrentOpacity() {
        currentOpacity = WallpaperManager.getWallpaperOpacity(requireContext())
        initialOpacity = currentOpacity
        opacitySeekBar.progress = currentOpacity
        updateOpacityText(currentOpacity)
    }

    private fun setupSeekBarListener() {
        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentOpacity = progress
                    updateOpacityText(progress)
                    // Apply opacity change immediately for real-time preview
                    applyOpacityChange(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupButtonListeners() {
        cancelButton.setOnClickListener {
            // Restore original opacity before dismissing
            if (currentOpacity != initialOpacity) {
                when (activity) {
                    is MainActivity -> (activity as MainActivity).previewWallpaperOpacity(initialOpacity)
                    is SettingsActivity -> {
                        // For settings activity, we need to restore through MainActivity
                        // Since SettingsActivity doesn't have preview capability
                        WallpaperManager.setWallpaperOpacity(requireContext(), initialOpacity)
                    }
                }
            }
            dismiss()
        }

        applyButton.setOnClickListener {
            // Save the current opacity setting
            WallpaperManager.setWallpaperOpacity(requireContext(), currentOpacity)

            // Refresh UI based on the activity type
            when (activity) {
                is MainActivity -> {
                    // For MainActivity, refresh wallpaper immediately
                    (activity as MainActivity).refreshWallpaper()
                }
                is SettingsActivity -> {
                    // For SettingsActivity, refresh the settings display
                    (activity as SettingsActivity).refreshSettingsDisplay()
                }
            }

            Toast.makeText(requireContext(), "Wallpaper opacity updated", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun updateOpacityText(opacity: Int) {
        opacityValueText.text = "Opacity: ${opacity}% (0% = opaque, 100% = transparent)"
    }

    private fun applyOpacityChange(opacity: Int) {
        // Apply opacity change for immediate preview
        when (activity) {
            is MainActivity -> {
                // For MainActivity, use preview method that doesn't save to SharedPreferences
                (activity as MainActivity).previewWallpaperOpacity(opacity)
            }
            is SettingsActivity -> {
                // For SettingsActivity, temporarily save and apply wallpaper
                // We'll restore it on cancel if needed
                WallpaperManager.setWallpaperOpacity(requireContext(), opacity)
                (activity as SettingsActivity).applyWallpaperBackground()
            }
        }
    }

    private fun applyFonts() {
        // Apply fonts to all text views
        val headerText = view?.findViewById<TextView>(R.id.headerTitle)
        headerText?.let { FontUtils.applyFontToTextView(requireContext(), it) }
        FontUtils.applyFontToTextView(requireContext(), opacityValueText)
    }

    companion object {
        fun newInstance(): WallpaperOpacityBottomSheet {
            return WallpaperOpacityBottomSheet()
        }
    }
}
