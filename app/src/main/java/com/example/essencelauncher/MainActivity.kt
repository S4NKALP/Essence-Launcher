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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    lateinit var viewPager : ViewPager2
    lateinit var appDrawerContainer: FrameLayout
    lateinit var hiddenAppsContainer: FrameLayout
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove fullscreen flags to allow proper keyboard handling
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        appDrawerContainer = findViewById(R.id.appDrawerContainer)
        hiddenAppsContainer = findViewById(R.id.hiddenAppsContainer)

        val fragments = listOf(LeftFragment(), HomeFragment(), RightFragment())
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        viewPager.setCurrentItem(1, false)

        setupGestureDetector()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 200
            private val SWIPE_VELOCITY_THRESHOLD = 200

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                // Only handle gestures when not in app drawer or hidden apps
                if (appDrawerContainer.visibility == View.VISIBLE || hiddenAppsContainer.visibility == View.VISIBLE) {
                    return false
                }

                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                Log.d("MainActivity", "Fling: diffX=$diffX, diffY=$diffY, velX=$velocityX, velY=$velocityY")

                // Check thresholds first
                if (abs(diffX) < SWIPE_THRESHOLD && abs(diffY) < SWIPE_THRESHOLD) {
                    return false
                }

                if (abs(velocityX) < SWIPE_VELOCITY_THRESHOLD && abs(velocityY) < SWIPE_VELOCITY_THRESHOLD) {
                    return false
                }

                // Determine primary direction with stronger bias
                if (abs(diffY) > abs(diffX) * 2) {
                    // Strong vertical swipe
                    if (abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) {
                            // Up swipe - open app drawer
                            Log.d("MainActivity", "Strong UP swipe detected")
                            openAppDrawer()
                            return true
                        } else {
                            // Down swipe - reserved for future implementation
                            Log.d("MainActivity", "Strong DOWN swipe detected")
                            onDownSwipe()
                            return true
                        }
                    }
                } else if (abs(diffX) > abs(diffY) * 2) {
                    // Strong horizontal swipe
                    if (abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Right swipe - go to left screen
                            Log.d("MainActivity", "Strong RIGHT swipe detected")
                            navigateToLeftScreen()
                            return true
                        } else {
                            // Left swipe - go to right screen
                            Log.d("MainActivity", "Strong LEFT swipe detected")
                            navigateToRightScreen()
                            return true
                        }
                    }
                }

                return false
            }
        })

        // Disable ViewPager2's user input and handle gestures manually
        viewPager.isUserInputEnabled = false

        // Set up touch listener on the ViewPager2 with multi-touch support
        viewPager.setOnTouchListener { _, event ->
            handleTouchEvent(event)
            true // Consume all touch events
        }
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        // Only handle gestures when not in app drawer or hidden apps
        if (appDrawerContainer.visibility == View.VISIBLE || hiddenAppsContainer.visibility == View.VISIBLE) {
            return false
        }

        // Check for pinch-out gesture
        if (event.pointerCount == 2) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Store initial positions for pinch gesture
                    initializePinchGesture(event)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Check if it's a pinch-out gesture
                    if (isPinchOutGesture(event)) {
                        Log.d("MainActivity", "Pinch-out gesture detected - opening hidden apps")
                        openHiddenApps()
                        return true
                    }
                }
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                    resetPinchGesture()
                }
            }
        }

        // Handle single-finger gestures with existing gesture detector
        return gestureDetector.onTouchEvent(event)
    }

    private var initialDistance = 0f
    private var hasInitialDistance = false
    private var gestureStartTime = 0L

    private fun initializePinchGesture(event: MotionEvent) {
        if (event.pointerCount == 2) {
            val x1 = event.getX(0)
            val y1 = event.getY(0)
            val x2 = event.getX(1)
            val y2 = event.getY(1)

            initialDistance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
            hasInitialDistance = true
            gestureStartTime = System.currentTimeMillis()
        }
    }

    private fun isPinchOutGesture(event: MotionEvent): Boolean {
        if (event.pointerCount != 2 || !hasInitialDistance) return false

        val x1 = event.getX(0)
        val y1 = event.getY(0)
        val x2 = event.getX(1)
        val y2 = event.getY(1)

        val currentDistance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
        val distanceIncrease = currentDistance - initialDistance

        // Check if the distance has increased significantly (pinch-out)
        val PINCH_THRESHOLD = 200f
        val MAX_GESTURE_TIME = 1000L // 1 second max

        val gestureTime = System.currentTimeMillis() - gestureStartTime

        if (distanceIncrease > PINCH_THRESHOLD && gestureTime < MAX_GESTURE_TIME) {
            resetPinchGesture()
            return true
        }

        return false
    }

    private fun resetPinchGesture() {
        hasInitialDistance = false
        initialDistance = 0f
        gestureStartTime = 0L
    }

    private fun navigateToLeftScreen() {
        val currentItem = viewPager.currentItem
        Log.d("MainActivity", "Navigating left from page $currentItem")

        when (currentItem) {
            2 -> viewPager.setCurrentItem(1, true) // From right to home
            1 -> viewPager.setCurrentItem(0, true) // From home to left
            0 -> Log.d("MainActivity", "Already at leftmost page")
        }
    }

    private fun navigateToRightScreen() {
        val currentItem = viewPager.currentItem
        Log.d("MainActivity", "Navigating right from page $currentItem")

        when (currentItem) {
            0 -> viewPager.setCurrentItem(1, true) // From left to home
            1 -> viewPager.setCurrentItem(2, true) // From home to right
            2 -> Log.d("MainActivity", "Already at rightmost page")
        }
    }

    private fun onDownSwipe() {
        Log.d("MainActivity", "Down swipe detected - opening notification panel")
        openNotificationPanel()
    }

    private fun openNotificationPanel() {
        try {
            // Method 1: Try to expand notification panel using reflection (works on most devices)
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
            expandMethod.invoke(statusBarService)
            Log.d("MainActivity", "Notification panel opened successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to open notification panel using reflection", e)
            try {
                // Method 2: Fallback - try using broadcast intent (limited compatibility)
                val intent = Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS")
                sendBroadcast(intent)

                // Try alternative method for newer Android versions
                val expandIntent = Intent()
                expandIntent.action = "android.intent.action.EXPAND_NOTIFICATIONS"
                sendBroadcast(expandIntent)
                Log.d("MainActivity", "Attempted to open notification panel using broadcast")
            } catch (e2: Exception) {
                Log.e("MainActivity", "All methods to open notification panel failed", e2)
                // Could show a toast to inform user that gesture is not supported on this device
            }
        }
    }

    fun openAppDrawer() {
        Log.d("MainActivity", "openAppDrawer called")
        try {
            // Set proper soft input mode for keyboard handling
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            // Hide ViewPager and show app drawer
            viewPager.visibility = View.GONE
            appDrawerContainer.visibility = View.VISIBLE

            val appDrawerFragment = AppDrawerFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.appDrawerContainer, appDrawerFragment)
                .addToBackStack("app_drawer")
                .commit()
            Log.d("MainActivity", "App drawer opened successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening app drawer", e)
        }
    }

    fun closeAppDrawer() {
        // Reset soft input mode to default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)

        // Show ViewPager and hide app drawer
        viewPager.visibility = View.VISIBLE
        appDrawerContainer.visibility = View.GONE

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    fun openHiddenApps() {
        Log.d("MainActivity", "openHiddenApps called")
        authenticateAndOpenHiddenApps()
    }

    private fun authenticateAndOpenHiddenApps() {
        if (!BiometricAuthManager.isAuthenticationAvailable(this)) {
            Toast.makeText(this, "Authentication not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current home fragment to use for authentication
        val homeFragment = supportFragmentManager.fragments
            .find { it is HomeFragment } as? HomeFragment

        if (homeFragment == null) {
            Toast.makeText(this, "Unable to authenticate at this time", Toast.LENGTH_SHORT).show()
            return
        }

        val authManager = BiometricAuthManager(homeFragment)
        authManager.authenticate("Hidden Apps", "access", object : BiometricAuthManager.AuthCallback {
            override fun onAuthSuccess() {
                // Open hidden apps
                openHiddenAppsAfterAuth()
            }

            override fun onAuthError(errorMessage: String) {
                Toast.makeText(this@MainActivity, "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthFailed() {
                Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openHiddenAppsAfterAuth() {
        Log.d("MainActivity", "openHiddenAppsAfterAuth called")
        try {
            // Hide ViewPager and show hidden apps
            viewPager.visibility = View.GONE
            hiddenAppsContainer.visibility = View.VISIBLE

            val hiddenAppsFragment = HiddenAppsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.hiddenAppsContainer, hiddenAppsFragment)
                .addToBackStack("hidden_apps")
                .commit()
            Log.d("MainActivity", "Hidden apps opened successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening hidden apps", e)
        }
    }

    fun closeHiddenApps() {
        Log.d("MainActivity", "closeHiddenApps called")
        // Show ViewPager and hide hidden apps
        viewPager.visibility = View.VISIBLE
        hiddenAppsContainer.visibility = View.GONE

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    fun refreshAppDrawer() {
        // Find and refresh the app drawer fragment if it's currently visible
        val appDrawerFragment = supportFragmentManager.fragments
            .find { it is AppDrawerFragment } as? AppDrawerFragment
        appDrawerFragment?.refreshApps()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Check which screen is currently open
            if (hiddenAppsContainer.visibility == View.VISIBLE) {
                closeHiddenApps()
            } else if (appDrawerContainer.visibility == View.VISIBLE) {
                closeAppDrawer()
            }
        } else {
            super.onBackPressed()
        }
    }
}
