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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class HiddenAppAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private var apps: List<AppInfo>,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<HiddenAppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hidden_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.appName.text = app.displayName

        // Show/hide lock icon based on locked status
        holder.lockIcon.visibility = if (app.isLocked) View.VISIBLE else View.GONE

        // Set click listener to launch app
        holder.itemView.setOnClickListener {
            if (app.isLocked) {
                authenticateAndLaunchApp(app)
            } else {
                launchApp(app.packageName)
            }
        }

        // Set long click listener for options
        holder.itemView.setOnLongClickListener {
            onLongClick(app)
            true
        }
    }

    override fun getItemCount() = apps.size

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    private fun authenticateAndLaunchApp(app: AppInfo) {
        if (!BiometricAuthManager.isAuthenticationAvailable(context)) {
            Toast.makeText(context, "Authentication not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val authManager = BiometricAuthManager(fragment)
        authManager.authenticate(app.displayName, object : BiometricAuthManager.AuthCallback {
            override fun onAuthSuccess() {
                launchApp(app.packageName)
            }

            override fun onAuthError(errorMessage: String) {
                Toast.makeText(context, "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthFailed() {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Handle exception if app cannot be launched
        }
    }
}
