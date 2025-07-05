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
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
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
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove fullscreen flags to allow proper keyboard handling
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        appDrawerContainer = findViewById(R.id.appDrawerContainer)

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

                // Only handle gestures when not in app drawer
                if (appDrawerContainer.visibility == View.VISIBLE) {
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

        // Set up touch listener on the ViewPager2
        viewPager.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true // Consume all touch events
        }
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
        Log.d("MainActivity", "Down swipe detected - reserved for future implementation")
        // TODO: Implement down swipe functionality later
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            closeAppDrawer()
        } else {
            super.onBackPressed()
        }
    }
}
