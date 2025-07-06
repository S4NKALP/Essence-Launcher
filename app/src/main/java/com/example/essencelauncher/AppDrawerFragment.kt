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

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs


class AppDrawerFragment : Fragment() {

    private lateinit var recentAppsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var playStoreIcon: ImageView
    private lateinit var webIcon: ImageView
    private lateinit var googleIcon: ImageView
    private lateinit var dashboardIcon: ImageView

    private lateinit var recentAppAdapter: RecentAppAdapter
    private lateinit var searchAppAdapter: AppAdapter
    private var allApps = mutableListOf<AppInfo>()
    private var recentApps = mutableListOf<AppInfo>()
    private var favoriteApps = mutableSetOf<String>()
    private var hiddenApps = mutableSetOf<String>()
    private var isSearchMode = false
    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_drawer, container, false)

        loadFavorites()
        loadHiddenApps()
        initViews(view)
        setupRecyclerView()
        setupSearchFunctionality()
        setupGestureDetector(view)
        loadApps()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Automatically show keyboard and focus on search
        showKeyboardAndFocus()
    }

    private fun initViews(view: View) {
        recentAppsRecyclerView = view.findViewById(R.id.recentAppsRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        playStoreIcon = view.findViewById(R.id.playStoreIcon)
        webIcon = view.findViewById(R.id.webIcon)
        googleIcon = view.findViewById(R.id.googleIcon)
        dashboardIcon = view.findViewById(R.id.dashboardIcon)

        loadIconsFromDrawables()
    }

    private fun setupRecyclerView() {
        // Setup recent apps adapter (no star buttons)
        recentAppAdapter = RecentAppAdapter(requireContext(), recentApps) { app ->
            showAppOptionsDialog(app)
        }

        // Setup search apps adapter (with star buttons)
        searchAppAdapter = AppAdapter(requireContext(), emptyList(), { app ->
            toggleFavorite(app)
        }) { app ->
            showAppOptionsDialog(app)
        }

        recentAppsRecyclerView.layoutManager = LinearLayoutManager(context)
        recentAppsRecyclerView.adapter = recentAppAdapter
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    enterSearchMode(query)
                } else {
                    exitSearchMode()
                }
            }
        })

        // Handle search action from keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    // Default action: show all apps with search query
                    enterSearchMode(query)
                }
                true
            } else {
                false
            }
        }

        playStoreIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            searchInPlayStore(query)
        }

        webIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            searchInWeb(query)
        }

        googleIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            searchInGoogleImages(query)
        }

        dashboardIcon.setOnClickListener {
            // Show all applications regardless of search query
            showAllApps()
        }
    }

    private fun loadApps() {
        val packageManager = requireContext().packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        allApps.clear()

        for (resolveInfo in resolveInfos) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val packageName = appInfo.packageName

            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val installTime = packageInfo.firstInstallTime
                val isFavorite = favoriteApps.contains(packageName)

                allApps.add(AppInfo(appName, packageName, installTime, isFavorite))
            } catch (e: Exception) {
                // Skip apps that can't be queried
            }
        }

        // Filter out hidden apps and sort by install time (most recent first) and take top 10
        recentApps.clear()
        recentApps.addAll(
            allApps.filter { !hiddenApps.contains(it.packageName) }
                .sortedByDescending { it.installTime }.take(10)
        )

        recentAppAdapter.updateApps(recentApps)
    }

    private fun enterSearchMode(query: String) {
        if (!isSearchMode) {
            isSearchMode = true
            recentAppsRecyclerView.adapter = searchAppAdapter
        }

        val filteredApps = allApps.filter { app ->
            app.name.contains(query, ignoreCase = true) && !hiddenApps.contains(app.packageName)
        }.map { app ->
            // Update favorite status based on current favorites set
            app.copy(isFavorite = favoriteApps.contains(app.packageName))
        }
        searchAppAdapter.updateApps(filteredApps)
    }

    private fun exitSearchMode() {
        if (isSearchMode) {
            isSearchMode = false
            recentAppsRecyclerView.adapter = recentAppAdapter
        }
    }

    private fun showAllApps() {
        // Clear search text and enter search mode to show all apps
        searchEditText.setText("")

        if (!isSearchMode) {
            isSearchMode = true
            recentAppsRecyclerView.adapter = searchAppAdapter
        }

        // Show all apps sorted alphabetically with favorite status, excluding hidden apps
        val allAppsWithFavorites = allApps.filter { !hiddenApps.contains(it.packageName) }
            .map { app ->
                app.copy(isFavorite = favoriteApps.contains(app.packageName))
            }.sortedBy { it.name }

        searchAppAdapter.updateApps(allAppsWithFavorites)
    }

    private fun toggleFavorite(app: AppInfo) {
        if (app.isFavorite) {
            // Check if we already have 10 favorites when trying to add a new one
            if (favoriteApps.size >= 10) {
                // Show a message that maximum favorites reached
                android.widget.Toast.makeText(
                    requireContext(),
                    "Maximum 10 favorite apps allowed",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                // Revert the star state
                app.isFavorite = false

                // Refresh the search results to show correct star state
                if (isSearchMode) {
                    val currentQuery = searchEditText.text.toString().trim()
                    if (currentQuery.isNotEmpty()) {
                        enterSearchMode(currentQuery)
                    }
                }
                return
            }
            favoriteApps.add(app.packageName)
        } else {
            favoriteApps.remove(app.packageName)
        }

        // Save favorites to SharedPreferences
        saveFavorites()

        // Update the corresponding app in allApps list
        allApps.find { it.packageName == app.packageName }?.isFavorite = app.isFavorite

        // If we're in search mode, refresh the search results to show updated star states
        if (isSearchMode) {
            val currentQuery = searchEditText.text.toString().trim()
            if (currentQuery.isNotEmpty()) {
                enterSearchMode(currentQuery)
            }
        }

        // Notify MainActivity about favorite change
        (activity as? MainActivity)?.let { mainActivity ->
            // Find HomeFragment and update favorites
            val homeFragment = mainActivity.supportFragmentManager.fragments
                .find { it is HomeFragment } as? HomeFragment
            homeFragment?.updateFavoriteApps(getFavoriteApps())
        }
    }

    private fun getFavoriteApps(): List<AppInfo> {
        return allApps.filter { it.isFavorite }
    }

    private fun searchInPlayStore(query: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (query.isNotEmpty()) {
                intent.data = Uri.parse("market://search?q=$query")
            } else {
                // Open Play Store main page if no query
                intent.data = Uri.parse("market://")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val intent = Intent(Intent.ACTION_VIEW)
            if (query.isNotEmpty()) {
                intent.data = Uri.parse("https://play.google.com/store/search?q=$query")
            } else {
                // Open Play Store main page if no query
                intent.data = Uri.parse("https://play.google.com/store")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun searchInWeb(query: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (query.isNotEmpty()) {
            intent.data = Uri.parse("https://www.google.com/search?q=$query")
        } else {
            // Open Google main page if no query
            intent.data = Uri.parse("https://www.google.com")
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun searchInGoogleImages(query: String) {
        try {
            // Try to open Google app
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity")

            if (query.isNotEmpty()) {
                // Add search query to the intent
                intent.putExtra("query", query)
                intent.action = Intent.ACTION_WEB_SEARCH
                intent.putExtra(android.app.SearchManager.QUERY, query)
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback: try to launch Google app with package manager
                val intent = context?.packageManager?.getLaunchIntentForPackage("com.google.android.googlequicksearchbox")
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (query.isNotEmpty()) {
                        intent.putExtra("query", query)
                        intent.action = Intent.ACTION_WEB_SEARCH
                        intent.putExtra(android.app.SearchManager.QUERY, query)
                    }
                    startActivity(intent)
                } else {
                    throw Exception("Google app not found")
                }
            } catch (e2: Exception) {
                // Final fallback: open in browser
                val intent = Intent(Intent.ACTION_VIEW)
                if (query.isNotEmpty()) {
                    intent.data = Uri.parse("https://www.google.com/search?q=$query")
                } else {
                    intent.data = Uri.parse("https://www.google.com")
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    private fun loadFavorites() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val favoritesString = prefs.getString("favorite_apps", "") ?: ""
        favoriteApps.clear()
        if (favoritesString.isNotEmpty()) {
            favoriteApps.addAll(favoritesString.split(",").filter { it.isNotEmpty() })
        }
    }

    private fun saveFavorites() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val favoritesString = favoriteApps.joinToString(",")
        prefs.edit().putString("favorite_apps", favoritesString).apply()
    }

    private fun loadHiddenApps() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val hiddenAppsString = prefs.getString("hidden_apps", "") ?: ""
        hiddenApps.clear()
        if (hiddenAppsString.isNotEmpty()) {
            hiddenApps.addAll(hiddenAppsString.split(",").filter { it.isNotEmpty() })
        }
    }

    fun refreshApps() {
        loadHiddenApps()
        loadApps()
    }

    private fun loadIconsFromDrawables() {
        // Load icons directly from drawable resources
        playStoreIcon.setImageResource(R.drawable.ic_play_store)
        webIcon.setImageResource(R.drawable.ic_web)
        googleIcon.setImageResource(R.drawable.ic_google)
        dashboardIcon.setImageResource(R.drawable.ic_apps)
    }

    private fun showKeyboardAndFocus() {
        // Post with delay to ensure view is fully laid out
        Handler(Looper.getMainLooper()).postDelayed({
            searchEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }

    private fun setupGestureDetector(view: View) {
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                // Only handle down swipe to close app drawer
                if (abs(diffY) > abs(diffX)) {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            // Down swipe - close app drawer
                            closeAppDrawer()
                            return true
                        }
                    }
                }
                return false
            }
        })

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun closeAppDrawer() {
        (activity as? MainActivity)?.closeAppDrawer()
    }

    private fun showAppOptionsDialog(app: AppInfo) {
        val options = arrayOf("Hide App", "App Info", "Uninstall")

        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle(app.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> hideApp(app)
                    1 -> openAppInfo(app.packageName)
                    2 -> uninstallApp(app.packageName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun hideApp(app: AppInfo) {
        hiddenApps.add(app.packageName)
        saveHiddenApps()

        // Remove from favorites if it was favorited
        if (favoriteApps.contains(app.packageName)) {
            favoriteApps.remove(app.packageName)
            saveFavorites()

            // Update home fragment favorites
            (activity as? MainActivity)?.let { mainActivity ->
                val homeFragment = mainActivity.supportFragmentManager.fragments
                    .find { it is HomeFragment } as? HomeFragment
                homeFragment?.updateFavoriteApps(getFavoriteApps())
            }
        }

        // Refresh the app list
        loadApps()

        // Notify hidden apps fragment if it exists
        (activity as? MainActivity)?.let { mainActivity ->
            val hiddenAppsFragment = mainActivity.supportFragmentManager.fragments
                .find { it is HiddenAppsFragment } as? HiddenAppsFragment
            hiddenAppsFragment?.hideApp(app.packageName)
        }

        android.widget.Toast.makeText(
            requireContext(),
            "${app.name} hidden",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun saveHiddenApps() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val hiddenAppsString = hiddenApps.joinToString(",")
        prefs.edit().putString("hidden_apps", hiddenAppsString).apply()
    }

    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Cannot open app info", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun uninstallApp(packageName: String) {
        try {
            // Check if it's a system app first
            val packageManager = requireContext().packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)

            if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) {
                android.widget.Toast.makeText(requireContext(), "Cannot uninstall system apps", android.widget.Toast.LENGTH_SHORT).show()
                return
            }

            // Try the standard uninstall intent
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // Check if there's an activity that can handle this intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback: try to open app settings where user can uninstall
                val settingsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                settingsIntent.data = Uri.parse("package:$packageName")
                settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                if (settingsIntent.resolveActivity(packageManager) != null) {
                    startActivity(settingsIntent)
                    android.widget.Toast.makeText(requireContext(), "Please use the uninstall button in app settings", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Cannot uninstall this app", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Cannot uninstall app: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
