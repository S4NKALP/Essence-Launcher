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

class MainActivity : AppCompatActivity() {

    lateinit var viewPager : ViewPager2
    lateinit var appDrawerContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        appDrawerContainer = findViewById(R.id.appDrawerContainer)

        val fragments = listOf(LeftFragment(), HomeFragment(), RightFragment())
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        viewPager.setCurrentItem(1, false)
    }

    fun openAppDrawer() {
        Log.d("MainActivity", "openAppDrawer called")
        try {
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
