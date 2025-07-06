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

class RecentAppAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private var apps: List<AppInfo>,
    private val onAppLongPress: ((AppInfo) -> Unit)? = null
) : RecyclerView.Adapter<RecentAppAdapter.RecentAppViewHolder>() {

    class RecentAppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameText: TextView = view.findViewById(R.id.appNameText)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentAppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_app, parent, false)
        return RecentAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentAppViewHolder, position: Int) {
        val app = apps[position]

        holder.appNameText.text = app.displayName

        // Show/hide lock icon based on locked status
        holder.lockIcon.visibility = if (app.isLocked) View.VISIBLE else View.GONE
        
        // Handle app click to launch
        holder.itemView.setOnClickListener {
            if (app.isLocked) {
                authenticateAndLaunchApp(app)
            } else {
                launchApp(app.packageName)
            }
        }

        // Handle app long press for uninstall/info options
        holder.itemView.setOnLongClickListener {
            onAppLongPress?.invoke(app)
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
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
