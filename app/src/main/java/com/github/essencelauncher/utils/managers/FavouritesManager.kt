/*
 * Portions of this file are from George Clensy
 * Copyright (c) 2025 George Clensy
 *
 * Modifications Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in this project for details.
 */

@file:Suppress("unused")

package com.github.essencelauncher.utils.managers

import android.content.Context
import androidx.core.content.edit
import com.github.essencelauncher.R
import com.github.essencelauncher.utils.getIntSetting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoriteAppsManager(context: Context) {

    private val context = context
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "FavoriteAppsPrefs"
        private const val FAVORITE_APPS_KEY = "FavoriteApps"
    }

    private fun saveFavoriteApps(favoriteApps: List<String>) {
        val json = gson.toJson(favoriteApps)
        sharedPreferences.edit() {
            putString(FAVORITE_APPS_KEY, json)
        }
    }

    fun getFavoriteApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addFavoriteApp(packageName: String): Boolean {
        val favoriteApps = getFavoriteApps().toMutableList()
        val maxFavoriteApps = getIntSetting(context, context.getString(R.string.MaxFavoriteApps), 5)

        if (packageName !in favoriteApps && favoriteApps.size < maxFavoriteApps) {
            favoriteApps.add(packageName)
            saveFavoriteApps(favoriteApps)
            return true
        }
        return false // Either already in favorites or limit reached
    }

    fun removeFavoriteApp(packageName: String) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (favoriteApps.remove(packageName)) {
            saveFavoriteApps(favoriteApps)
        }
    }

    fun isAppFavorite(packageName: String): Boolean {
        return packageName in getFavoriteApps()
    }

    fun getFavoriteIndex(packageName: String): Int {
        return getFavoriteApps().indexOf(packageName)
    }

    fun reorderFavoriteApps(fromIndex: Int, toIndex: Int) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (fromIndex in favoriteApps.indices && toIndex in favoriteApps.indices) {
            val item = favoriteApps.removeAt(fromIndex)
            favoriteApps.add(toIndex, item)
            saveFavoriteApps(favoriteApps)
        }
    }

    fun getFavoriteAppsInOrder(): List<String> {
        return getFavoriteApps()
    }
}
