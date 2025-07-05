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
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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
import java.io.IOException

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
    private var isSearchMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_drawer, container, false)

        loadFavorites()
        initViews(view)
        setupRecyclerView()
        setupSearchFunctionality()
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

        loadIconsFromAssets()
    }

    private fun setupRecyclerView() {
        // Setup recent apps adapter (no star buttons)
        recentAppAdapter = RecentAppAdapter(requireContext(), recentApps)

        // Setup search apps adapter (with star buttons)
        searchAppAdapter = AppAdapter(requireContext(), emptyList()) { app ->
            toggleFavorite(app)
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
            if (query.isNotEmpty()) {
                searchInPlayStore(query)
            }
        }

        webIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchInWeb(query)
            }
        }

        googleIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchInGoogleImages(query)
            }
        }

        dashboardIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                enterSearchMode(query)
            }
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

        // Sort by install time (most recent first) and take top 10
        recentApps.clear()
        recentApps.addAll(
            allApps.sortedByDescending { it.installTime }.take(10)
        )

        recentAppAdapter.updateApps(recentApps)
    }

    private fun enterSearchMode(query: String) {
        if (!isSearchMode) {
            isSearchMode = true
            recentAppsRecyclerView.adapter = searchAppAdapter
        }

        val filteredApps = allApps.filter { app ->
            app.name.contains(query, ignoreCase = true)
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
            intent.data = Uri.parse("market://search?q=$query")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/search?q=$query")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun searchInWeb(query: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://www.google.com/search?q=$query")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun searchInGoogleImages(query: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://www.google.com/search?q=$query&tbm=isch")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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

    private fun loadIconsFromAssets() {
        try {
            // Load Play Store icon
            val playStoreInputStream = requireContext().assets.open("apps/playstore.png")
            val playStoreBitmap = BitmapFactory.decodeStream(playStoreInputStream)
            playStoreIcon.setImageDrawable(BitmapDrawable(resources, playStoreBitmap))
            playStoreInputStream.close()

            // Load Web icon
            val webInputStream = requireContext().assets.open("apps/web.png")
            val webBitmap = BitmapFactory.decodeStream(webInputStream)
            webIcon.setImageDrawable(BitmapDrawable(resources, webBitmap))
            webInputStream.close()

            // Load Google icon
            val googleInputStream = requireContext().assets.open("apps/google.png")
            val googleBitmap = BitmapFactory.decodeStream(googleInputStream)
            googleIcon.setImageDrawable(BitmapDrawable(resources, googleBitmap))
            googleInputStream.close()

            // Load Dashboard icon
            val dashboardInputStream = requireContext().assets.open("apps/dashboard.png")
            val dashboardBitmap = BitmapFactory.decodeStream(dashboardInputStream)
            dashboardIcon.setImageDrawable(BitmapDrawable(resources, dashboardBitmap))
            dashboardInputStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
            // Fallback to drawable icons if assets fail to load
            playStoreIcon.setImageResource(R.drawable.ic_play_store)
            webIcon.setImageResource(R.drawable.ic_web)
            googleIcon.setImageResource(R.drawable.ic_image)
            dashboardIcon.setImageResource(R.drawable.ic_apps)
        }
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
}
