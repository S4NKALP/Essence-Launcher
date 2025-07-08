/*
 * Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information.
 */
package com.github.essencelauncher.utils.managers

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manager class for handling locked apps functionality
 * Similar to FavoriteAppsManager and HiddenAppsManager
 * 
 * @author Essence Launcher Team
 */
class LockedAppsManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "LockedAppsPrefs"
        private const val LOCKED_APPS_KEY = "LockedApps"
    }

    private fun saveLockedApps(lockedApps: List<String>) {
        val json = gson.toJson(lockedApps)
        sharedPreferences.edit {
            putString(LOCKED_APPS_KEY, json)
        }
    }

    fun getLockedApps(): List<String> {
        val json = sharedPreferences.getString(LOCKED_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addLockedApp(packageName: String) {
        val lockedApps = getLockedApps().toMutableList()
        if (packageName !in lockedApps) {
            lockedApps.add(packageName)
            saveLockedApps(lockedApps)
        }
    }

    fun removeLockedApp(packageName: String) {
        val lockedApps = getLockedApps().toMutableList()
        if (lockedApps.remove(packageName)) {
            saveLockedApps(lockedApps)
        }
    }

    fun isAppLocked(packageName: String): Boolean {
        val lockedApps = getLockedApps()
        return packageName in lockedApps
    }
}
