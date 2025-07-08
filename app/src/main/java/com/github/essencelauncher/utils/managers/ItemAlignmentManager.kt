@file:Suppress("unused")

package com.github.essencelauncher.utils.managers

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.core.content.edit

/**
 * Manager for handling per-item alignment settings for home screen items
 */
class ItemAlignmentManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ItemAlignmentPrefs"
        private const val CLOCK_ALIGNMENT_KEY = "ClockAlignment"
        private const val FAVORITE_APPS_ALIGNMENT_KEY = "FavoriteAppsAlignment"
        private const val BOTTOM_DOCK_ALIGNMENT_KEY = "BottomDockAlignment"

        // Alignment constants
        const val ALIGNMENT_LEFT = 0
        const val ALIGNMENT_CENTER = 1
        const val ALIGNMENT_RIGHT = 2
    }

    // Per-app alignment methods removed - now using global favorite apps alignment only

    /**
     * Set clock alignment
     */
    fun setClockAlignment(alignment: Int) {
        sharedPreferences.edit {
            putInt(CLOCK_ALIGNMENT_KEY, alignment)
        }
    }

    /**
     * Get clock alignment
     */
    fun getClockAlignment(): Int {
        return sharedPreferences.getInt(CLOCK_ALIGNMENT_KEY, ALIGNMENT_CENTER)
    }

    /**
     * Get clock alignment as Alignment.Horizontal
     */
    fun getClockAlignmentAsHorizontal(): Alignment.Horizontal {
        return when (getClockAlignment()) {
            ALIGNMENT_LEFT -> Alignment.Start
            ALIGNMENT_RIGHT -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    }

    /**
     * Set favorite apps default alignment
     */
    fun setFavoriteAppsAlignment(alignment: Int) {
        sharedPreferences.edit {
            putInt(FAVORITE_APPS_ALIGNMENT_KEY, alignment)
        }
    }

    /**
     * Get favorite apps default alignment
     */
    fun getFavoriteAppsAlignment(): Int {
        return sharedPreferences.getInt(FAVORITE_APPS_ALIGNMENT_KEY, ALIGNMENT_CENTER)
    }

    /**
     * Get favorite apps alignment as Alignment.Horizontal
     */
    fun getFavoriteAppsAlignmentAsHorizontal(): Alignment.Horizontal {
        return when (getFavoriteAppsAlignment()) {
            ALIGNMENT_LEFT -> Alignment.Start
            ALIGNMENT_RIGHT -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    }

    /**
     * Set bottom dock alignment
     */
    fun setBottomDockAlignment(alignment: Int) {
        sharedPreferences.edit {
            putInt(BOTTOM_DOCK_ALIGNMENT_KEY, alignment)
        }
    }

    /**
     * Get bottom dock alignment
     */
    fun getBottomDockAlignment(): Int {
        return sharedPreferences.getInt(BOTTOM_DOCK_ALIGNMENT_KEY, ALIGNMENT_CENTER)
    }

    /**
     * Get bottom dock alignment as Alignment.Horizontal
     */
    fun getBottomDockAlignmentAsHorizontal(): Alignment.Horizontal {
        return when (getBottomDockAlignment()) {
            ALIGNMENT_LEFT -> Alignment.Start
            ALIGNMENT_RIGHT -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    }

    /**
     * Clear all alignment settings
     */
    fun clearAllAlignments() {
        sharedPreferences.edit {
            remove(CLOCK_ALIGNMENT_KEY)
            remove(FAVORITE_APPS_ALIGNMENT_KEY)
            remove(BOTTOM_DOCK_ALIGNMENT_KEY)
        }
    }
}
