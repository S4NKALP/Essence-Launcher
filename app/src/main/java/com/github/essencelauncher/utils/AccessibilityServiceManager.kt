/*
 * Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information.
 */
package com.github.essencelauncher.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.github.essencelauncher.R
import com.github.essencelauncher.services.DoubleTapLockAccessibilityService

/**
 * Utility class for managing accessibility service permissions and state
 */
object AccessibilityServiceManager {

    /**
     * Check if the accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return DoubleTapLockAccessibilityService.isAccessibilityServiceEnabled(context)
    }

    /**
     * Request accessibility permission with user dialog
     */
    fun requestAccessibilityPermission(context: Context, onResult: (Boolean) -> Unit) {
        if (isAccessibilityServiceEnabled(context)) {
            onResult(true)
            return
        }

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.accessibility_permission_required))
            .setMessage(context.getString(R.string.accessibility_permission_message))
            .setPositiveButton(context.getString(R.string.open_accessibility_settings)) { _, _ ->
                openAccessibilitySettings(context)
                // Note: We can't directly know when user enables the service
                // The calling code should check the service status when the user returns
                onResult(false)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onResult(false)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Open accessibility settings for the user to enable the service
     */
    fun openAccessibilitySettings(context: Context) {
        DoubleTapLockAccessibilityService.openAccessibilitySettings(context)
    }

    /**
     * Handle the double tap lock screen setting change
     * This will enable/disable the accessibility service overlay
     */
    fun handleDoubleTapLockScreenSettingChange(context: Context, enabled: Boolean) {
        if (enabled) {
            // Check if accessibility service is enabled
            if (!isAccessibilityServiceEnabled(context)) {
                // Request permission
                requestAccessibilityPermission(context) { granted ->
                    if (!granted) {
                        // If permission not granted, disable the setting
                        setBooleanSetting(context, context.getString(R.string.DoubleTapLockScreen), false)
                    }
                }
            }
        } else {
            // Setting disabled, no need to do anything special
            // The service will check the setting and disable overlay automatically
        }
    }

    /**
     * Show a dialog informing user that accessibility service was disabled
     */
    fun showAccessibilityServiceDisabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.accessibility_permission_required))
            .setMessage(context.getString(R.string.accessibility_service_disabled))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /**
     * Check if accessibility service is enabled and show dialog if not
     * Returns true if service is enabled, false otherwise
     */
    fun checkAndPromptAccessibilityService(context: Context): Boolean {
        if (!isAccessibilityServiceEnabled(context)) {
            showAccessibilityServiceDisabledDialog(context)
            return false
        }
        return true
    }

    /**
     * Notify the accessibility service to update its overlay based on current settings
     */
    fun notifyServiceSettingsChanged(context: Context) {
        // The service will check settings in its overlay setup method
        // This is called when settings change to trigger overlay update
    }
}
