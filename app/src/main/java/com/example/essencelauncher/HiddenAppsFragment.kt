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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsFragment : Fragment() {

    private lateinit var hiddenAppsRecyclerView: RecyclerView
    private lateinit var hiddenAppsSearchEditText: EditText
    private lateinit var clearSearchIcon: ImageView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var hiddenAppsCount: TextView

    private lateinit var hiddenAppAdapter: HiddenAppAdapter
    private var allApps = mutableListOf<AppInfo>()
    private var hiddenApps = mutableListOf<AppInfo>()
    private var hiddenAppPackages = mutableSetOf<String>()
    private var customAppNames = mutableMapOf<String, String>()
    private var lockedApps = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hidden_apps, container, false)

        loadHiddenApps()
        loadCustomNames()
        loadLockedApps()
        initViews(view)
        setupRecyclerView()
        setupSearchFunctionality()

        loadApps()

        return view
    }

    private fun initViews(view: View) {
        hiddenAppsRecyclerView = view.findViewById(R.id.hiddenAppsRecyclerView)
        hiddenAppsSearchEditText = view.findViewById(R.id.hiddenAppsSearchEditText)
        clearSearchIcon = view.findViewById(R.id.clearSearchIcon)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        hiddenAppsCount = view.findViewById(R.id.hiddenAppsCount)
    }

    private fun setupRecyclerView() {
        // Setup hidden apps adapter (no star buttons for hidden apps)
        hiddenAppAdapter = HiddenAppAdapter(requireContext(), this, emptyList()) { app ->
            showAppOptionsDialog(app)
        }

        hiddenAppsRecyclerView.layoutManager = LinearLayoutManager(context)
        hiddenAppsRecyclerView.adapter = hiddenAppAdapter
    }

    private fun setupSearchFunctionality() {
        hiddenAppsSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterHiddenApps(query)
                clearSearchIcon.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
        })

        clearSearchIcon.setOnClickListener {
            hiddenAppsSearchEditText.setText("")
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
                val customName = customAppNames[packageName]
                val isLocked = lockedApps.contains(packageName)

                allApps.add(AppInfo(appName, packageName, installTime, false, customName, isLocked))
            } catch (e: Exception) {
                // Skip apps that can't be queried
            }
        }

        updateHiddenAppsList()
    }

    private fun updateHiddenAppsList() {
        hiddenApps.clear()
        hiddenApps.addAll(
            allApps.filter { hiddenAppPackages.contains(it.packageName) }
                .sortedBy { it.displayName }
        )

        updateUI()
    }

    private fun filterHiddenApps(query: String) {
        val filteredApps = if (query.isEmpty()) {
            hiddenApps
        } else {
            hiddenApps.filter { app ->
                app.name.contains(query, ignoreCase = true) || app.displayName.contains(query, ignoreCase = true)
            }
        }

        hiddenAppAdapter.updateApps(filteredApps)
        updateEmptyState(filteredApps.isEmpty())
    }

    private fun updateUI() {
        hiddenAppsCount.text = "${hiddenApps.size} apps"
        filterHiddenApps(hiddenAppsSearchEditText.text.toString().trim())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (hiddenApps.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            hiddenAppsRecyclerView.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            hiddenAppsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun unhideApp(app: AppInfo) {
        hiddenAppPackages.remove(app.packageName)
        saveHiddenApps()
        updateHiddenAppsList()

        // Notify MainActivity to refresh app drawer
        (activity as? MainActivity)?.refreshAppDrawer()

        android.widget.Toast.makeText(
            requireContext(),
            "${app.name} unhidden",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun hideApp(packageName: String) {
        hiddenAppPackages.add(packageName)
        saveHiddenApps()
        updateHiddenAppsList()
    }

    private fun loadHiddenApps() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val hiddenAppsString = prefs.getString("hidden_apps", "") ?: ""
        hiddenAppPackages.clear()
        if (hiddenAppsString.isNotEmpty()) {
            hiddenAppPackages.addAll(hiddenAppsString.split(",").filter { it.isNotEmpty() })
        }
    }

    private fun saveHiddenApps() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val hiddenAppsString = hiddenAppPackages.joinToString(",")
        prefs.edit().putString("hidden_apps", hiddenAppsString).apply()
    }

    fun getHiddenAppPackages(): Set<String> {
        return hiddenAppPackages.toSet()
    }

    private fun loadCustomNames() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val customNamesString = prefs.getString("custom_app_names", "") ?: ""
        customAppNames.clear()
        if (customNamesString.isNotEmpty()) {
            customNamesString.split("|").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    customAppNames[parts[0]] = parts[1]
                }
            }
        }
    }

    private fun loadLockedApps() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val lockedAppsString = prefs.getString("locked_apps", "") ?: ""
        lockedApps.clear()
        if (lockedAppsString.isNotEmpty()) {
            lockedApps.addAll(lockedAppsString.split(",").filter { it.isNotEmpty() })
        }
    }



    private fun showAppOptionsDialog(app: AppInfo) {
        val lockOption = if (app.isLocked) "Unlock App" else "Lock App"
        val options = arrayOf("Rename", lockOption, "Unhide", "App Info", "Uninstall")

        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle(app.displayName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameDialog(app)
                    1 -> toggleAppLock(app)
                    2 -> unhideApp(app)
                    3 -> openAppInfo(app.packageName)
                    4 -> uninstallApp(app.packageName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameDialog(app: AppInfo) {
        val editText = EditText(requireContext())
        editText.setText(app.customName ?: app.name)
        editText.selectAll()

        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Rename App")
            .setMessage("Enter a new name for ${app.name}")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                // Get the app drawer fragment and update the custom name
                (activity as? MainActivity)?.let { mainActivity ->
                    val appDrawerFragment = mainActivity.supportFragmentManager.fragments
                        .find { it is AppDrawerFragment } as? AppDrawerFragment
                    appDrawerFragment?.setCustomAppName(app.packageName, newName)

                    // Update the current app in the list
                    app.customName = if (newName.isBlank()) null else newName
                    hiddenAppAdapter.updateApps(hiddenApps)
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Reset") { _, _ ->
                // Get the app drawer fragment and reset the custom name
                (activity as? MainActivity)?.let { mainActivity ->
                    val appDrawerFragment = mainActivity.supportFragmentManager.fragments
                        .find { it is AppDrawerFragment } as? AppDrawerFragment
                    appDrawerFragment?.setCustomAppName(app.packageName, "")

                    // Update the current app in the list
                    app.customName = null
                    hiddenAppAdapter.updateApps(hiddenApps)
                }
            }
            .show()
    }

    private fun toggleAppLock(app: AppInfo) {
        if (app.isLocked) {
            // Unlock the app - require authentication
            authenticateAndUnlockApp(app)
        } else {
            // Lock the app
            (activity as? MainActivity)?.let { mainActivity ->
                val appDrawerFragment = mainActivity.supportFragmentManager.fragments
                    .find { it is AppDrawerFragment } as? AppDrawerFragment
                appDrawerFragment?.setAppLocked(app.packageName, true)

                // Update the current app in the list
                app.isLocked = true
                hiddenAppAdapter.updateApps(hiddenApps)

                android.widget.Toast.makeText(requireContext(), "${app.displayName} locked", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateAndUnlockApp(app: AppInfo) {
        if (!BiometricAuthManager.isAuthenticationAvailable(requireContext())) {
            android.widget.Toast.makeText(requireContext(), "Authentication not available on this device", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val authManager = BiometricAuthManager(this)
        authManager.authenticate(app.displayName, "unlock", object : BiometricAuthManager.AuthCallback {
            override fun onAuthSuccess() {
                (activity as? MainActivity)?.let { mainActivity ->
                    val appDrawerFragment = mainActivity.supportFragmentManager.fragments
                        .find { it is AppDrawerFragment } as? AppDrawerFragment
                    appDrawerFragment?.setAppLocked(app.packageName, false)

                    // Update the current app in the list
                    app.isLocked = false
                    hiddenAppAdapter.updateApps(hiddenApps)

                    android.widget.Toast.makeText(requireContext(), "${app.displayName} unlocked", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthError(errorMessage: String) {
                android.widget.Toast.makeText(requireContext(), "Authentication error: $errorMessage", android.widget.Toast.LENGTH_SHORT).show()
            }

            override fun onAuthFailed() {
                android.widget.Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
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
            val packageManager = requireContext().packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)

            if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) {
                android.widget.Toast.makeText(requireContext(), "Cannot uninstall system apps", android.widget.Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
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
