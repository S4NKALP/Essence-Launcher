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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeContainer: LinearLayout
    private lateinit var batteryContainer: LinearLayout
    private lateinit var batteryPercentageText: TextView
    private lateinit var batteryIcon: ImageView
    private lateinit var searchLabel: LinearLayout
    private lateinit var favoriteAppsContainer: LinearLayout
    private lateinit var phoneButton: ImageView
    private lateinit var messageButton: ImageView
    private lateinit var gestureDetector: GestureDetector
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeUpdateRunnable: Runnable

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryInfo(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        timeTextView = view.findViewById(R.id.timeTextView)
        dateTextView = view.findViewById(R.id.dateTextView)
        timeContainer = view.findViewById(R.id.timeContainer)
        batteryContainer = view.findViewById(R.id.batteryContainer)
        batteryPercentageText = view.findViewById(R.id.batteryPercentageText)
        batteryIcon = view.findViewById(R.id.batteryIcon)
        searchLabel = view.findViewById(R.id.searchLabel)
        favoriteAppsContainer = view.findViewById(R.id.favoriteAppsContainer)
        phoneButton = view.findViewById(R.id.phoneButton)
        messageButton = view.findViewById(R.id.messageButton)

        setupTimeUpdate()
        setupTimeClickListener()
        setupBatteryClickListener()
        setupCommunicationClickListeners()

        return view
    }

    private fun setupTimeUpdate() {
        timeUpdateRunnable = object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, 1000) // Update every second
            }
        }
    }

    private fun setupTimeClickListener() {
        timeContainer.setOnClickListener {
            openClockApp()
        }
    }

    private fun setupBatteryClickListener() {
        batteryContainer.setOnClickListener {
            openBatterySettings()
        }
    }

    private fun setupCommunicationClickListeners() {
        phoneButton.setOnClickListener {
            openPhoneApp()
        }

        messageButton.setOnClickListener {
            openMessageApp()
        }
    }





    private fun openAppDrawer() {
        Log.d("HomeFragment", "Opening app drawer")
        try {
            (activity as? MainActivity)?.openAppDrawer()
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error in openAppDrawer", e)
            Toast.makeText(context, "Cannot open app drawer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openClockApp() {
        try {
            // Try to open the default clock app
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback: try to open any clock app
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.setClassName("com.google.android.deskclock", "com.android.deskclock.DeskClock")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e2: Exception) {
                try {
                    // Another fallback: try Samsung clock
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.setClassName("com.sec.android.app.clockpackage", "com.sec.android.app.clockpackage.ClockPackage")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } catch (e3: Exception) {
                    // Show a message if no clock app is found
                    Toast.makeText(context, "No clock app found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Cannot open battery settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openPhoneApp() {
        try {
            // Try to open the default phone app using the dialer intent
            val intent = Intent(Intent.ACTION_DIAL)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback: try to open any phone app
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.setClassName("com.google.android.dialer", "com.android.dialer.DialtactsActivity")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e2: Exception) {
                try {
                    // Another fallback: try Samsung phone app
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.setClassName("com.samsung.android.dialer", "com.samsung.android.dialer.DialtactsActivity")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "No phone app found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openMessageApp() {
        try {
            // Try to open the default messaging app
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.type = "vnd.android-dir/mms-sms"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback: try Google Messages
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.setClassName("com.google.android.apps.messaging", "com.google.android.apps.messaging.ui.ConversationListActivity")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e2: Exception) {
                try {
                    // Another fallback: try Samsung Messages
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.setClassName("com.samsung.android.messaging", "com.android.mms.ui.ConversationList")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateBatteryInfo(intent: Intent?) {
        if (intent == null) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        if (level >= 0 && scale > 0) {
            val batteryPct = (level * 100 / scale)
            batteryPercentageText.text = "$batteryPct%"

            // Check if charging
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL

            // Update battery icon based on percentage and charging status
            val iconRes = if (isCharging) {
                // Use charging icon for all charging states
                R.drawable.ic_battery_charging
            } else {
                // Use different battery level icons based on percentage
                when {
                    batteryPct >= 90 -> R.drawable.ic_battery_full
                    batteryPct >= 60 -> R.drawable.ic_battery_high
                    batteryPct >= 30 -> R.drawable.ic_battery_medium
                    batteryPct >= 15 -> R.drawable.ic_battery_low
                    else -> R.drawable.ic_battery_empty
                }
            }
            batteryIcon.setImageResource(iconRes)
        }
    }

    private fun updateTime() {
        val calendar = Calendar.getInstance()

        // Format time (24-hour format)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(calendar.time)
        timeTextView.text = timeString

        // Format date
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        dateTextView.text = dateString
    }

    override fun onResume() {
        super.onResume()
        updateTime() // Update immediately when fragment becomes visible
        handler.post(timeUpdateRunnable) // Start the timer

        // Register battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context?.registerReceiver(batteryReceiver, filter)

        // Get initial battery status
        val batteryStatus = context?.registerReceiver(null, filter)
        updateBatteryInfo(batteryStatus)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeUpdateRunnable) // Stop the timer to save battery

        // Unregister battery receiver
        try {
            context?.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    fun updateFavoriteApps(favoriteApps: List<AppInfo>) {
        favoriteAppsContainer.removeAllViews()

        for (app in favoriteApps) {
            val favoriteView = LayoutInflater.from(context)
                .inflate(R.layout.item_favorite_app, favoriteAppsContainer, false)

            val favoriteAppName = favoriteView.findViewById<TextView>(R.id.favoriteAppName)
            val lockIcon = favoriteView.findViewById<ImageView>(R.id.lockIcon)

            favoriteAppName.text = app.displayName
            lockIcon.visibility = if (app.isLocked) View.VISIBLE else View.GONE

            favoriteView.setOnClickListener {
                if (app.isLocked) {
                    authenticateAndLaunchApp(app)
                } else {
                    launchApp(app.packageName)
                }
            }

            favoriteView.setOnLongClickListener {
                showAppOptionsDialog(app)
                true
            }

            favoriteAppsContainer.addView(favoriteView)
        }
    }

    private fun authenticateAndLaunchApp(app: AppInfo) {
        if (!BiometricAuthManager.isAuthenticationAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Authentication not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val authManager = BiometricAuthManager(this)
        authManager.authenticate(app.displayName, object : BiometricAuthManager.AuthCallback {
            override fun onAuthSuccess() {
                launchApp(app.packageName)
            }

            override fun onAuthError(errorMessage: String) {
                Toast.makeText(requireContext(), "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthFailed() {
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = context?.packageManager?.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppOptionsDialog(app: AppInfo) {
        val lockOption = if (app.isLocked) "Unlock App" else "Lock App"
        val options = arrayOf("Rename", lockOption, "App Info", "Uninstall")

        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle(app.displayName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameDialog(app)
                    1 -> toggleAppLock(app)
                    2 -> openAppInfo(app.packageName)
                    3 -> uninstallApp(app.packageName)
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
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Reset") { _, _ ->
                // Get the app drawer fragment and reset the custom name
                (activity as? MainActivity)?.let { mainActivity ->
                    val appDrawerFragment = mainActivity.supportFragmentManager.fragments
                        .find { it is AppDrawerFragment } as? AppDrawerFragment
                    appDrawerFragment?.setCustomAppName(app.packageName, "")
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

                Toast.makeText(requireContext(), "${app.displayName} locked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateAndUnlockApp(app: AppInfo) {
        if (!BiometricAuthManager.isAuthenticationAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Authentication not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val authManager = BiometricAuthManager(this)
        authManager.authenticate(app.displayName, "unlock", object : BiometricAuthManager.AuthCallback {
            override fun onAuthSuccess() {
                (activity as? MainActivity)?.let { mainActivity ->
                    val appDrawerFragment = mainActivity.supportFragmentManager.fragments
                        .find { it is AppDrawerFragment } as? AppDrawerFragment
                    appDrawerFragment?.setAppLocked(app.packageName, false)

                    Toast.makeText(requireContext(), "${app.displayName} unlocked", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthError(errorMessage: String) {
                Toast.makeText(requireContext(), "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthFailed() {
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open app info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uninstallApp(packageName: String) {
        try {
            // Check if it's a system app first
            val packageManager = requireContext().packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)

            if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) {
                Toast.makeText(context, "Cannot uninstall system apps", Toast.LENGTH_SHORT).show()
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
                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                settingsIntent.data = Uri.parse("package:$packageName")
                settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                if (settingsIntent.resolveActivity(packageManager) != null) {
                    startActivity(settingsIntent)
                    Toast.makeText(context, "Please use the uninstall button in app settings", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Cannot uninstall this app", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot uninstall app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
