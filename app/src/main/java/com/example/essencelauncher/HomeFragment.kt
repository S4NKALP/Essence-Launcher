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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private lateinit var appDrawerTrigger: LinearLayout
    private lateinit var favoriteAppsContainer: LinearLayout
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
        appDrawerTrigger = view.findViewById(R.id.appDrawerTrigger)
        favoriteAppsContainer = view.findViewById(R.id.favoriteAppsContainer)

        setupTimeUpdate()
        setupTimeClickListener()
        setupBatteryClickListener()
        setupAppDrawerGesture(view)
        setupAppDrawerTrigger()

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

    private fun setupAppDrawerGesture(view: View) {
        var startY = 0f
        var startTime = 0L

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    startTime = System.currentTimeMillis()
                    false // Don't consume
                }
                MotionEvent.ACTION_UP -> {
                    val endY = event.y
                    val endTime = System.currentTimeMillis()
                    val diffY = startY - endY
                    val diffTime = endTime - startTime
                    val velocity = diffY / diffTime

                    // Check for swipe up gesture from bottom area
                    val screenHeight = view.height
                    val startFromBottom = startY > screenHeight * 0.7f

                    if (startFromBottom && diffY > 200 && velocity > 0.5f && diffTime < 500) {
                        openAppDrawer()
                        true // Consume this event
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun setupAppDrawerTrigger() {
        appDrawerTrigger.setOnClickListener { view ->
            Log.d("HomeFragment", "App drawer trigger clicked")
            try {
                // Prevent any system behavior
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                openAppDrawer()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error opening app drawer", e)
                Toast.makeText(context, "Error opening app drawer", Toast.LENGTH_SHORT).show()
            }
        }

        // Also handle long press to prevent system behavior
        appDrawerTrigger.setOnLongClickListener {
            Log.d("HomeFragment", "App drawer trigger long clicked")
            openAppDrawer()
            true // Consume the event
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
                when {
                    batteryPct >= 90 -> R.drawable.ic_battery_horizontal_charging_full
                    batteryPct >= 60 -> R.drawable.ic_battery_horizontal_charging_80
                    batteryPct >= 30 -> R.drawable.ic_battery_horizontal_charging_60
                    batteryPct >= 15 -> R.drawable.ic_battery_horizontal_charging_30
                    else -> R.drawable.ic_battery_horizontal_charging_low
                }
            } else {
                when {
                    batteryPct >= 90 -> R.drawable.ic_battery_horizontal_full
                    batteryPct >= 60 -> R.drawable.ic_battery_horizontal_80
                    batteryPct >= 30 -> R.drawable.ic_battery_horizontal_60
                    batteryPct >= 15 -> R.drawable.ic_battery_horizontal_30
                    else -> R.drawable.ic_battery_horizontal_low
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
            favoriteAppName.text = app.name

            favoriteView.setOnClickListener {
                launchApp(app.packageName)
            }

            favoriteAppsContainer.addView(favoriteView)
        }
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
}
