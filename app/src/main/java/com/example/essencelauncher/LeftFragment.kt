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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class LeftFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_left, container, false)
        // Don't apply wallpaper background - inherit from main container
        return view
    }

    private fun applyWallpaperBackground(view: View) {
        // Apply wallpaper background to the root view
        val rootView = view.findViewById<LinearLayout>(R.id.leftFragmentRoot)
        WallpaperManager.applyWallpaperBackground(requireContext(), rootView)
    }

    fun refreshWallpaper() {
        // Refresh wallpaper background on the root view
        view?.let { applyWallpaperBackground(it) }
    }

    fun previewWallpaperOpacity(previewOpacity: Int) {
        // Apply wallpaper with preview opacity on the root view
        view?.let { v ->
            val rootView = v.findViewById<LinearLayout>(R.id.leftFragmentRoot)
            WallpaperManager.applyWallpaperBackgroundWithOpacity(requireContext(), rootView, previewOpacity)
        }
    }

    override fun onResume() {
        super.onResume()
        // Don't refresh wallpaper background - inherit from main container
    }
}
