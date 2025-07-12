/*
 * Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information.
 */
package com.github.essencelauncher.services

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.graphics.PixelFormat
import com.github.essencelauncher.R
import com.github.essencelauncher.utils.getBooleanSetting
import com.github.essencelauncher.utils.listener.GestureManager
import com.github.essencelauncher.utils.listener.GestureAdapter

/**
 * Minimal accessibility service for screen locking functionality only
 *
 * This service is designed to be as minimal as possible to avoid TalkBack-like features.
 * It only provides the ability to lock the screen when requested.
 */
class DoubleTapLockAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private var serviceInstance: DoubleTapLockAccessibilityService? = null

        /**
         * Lock the screen if the service is available and enabled
         */
        fun lockScreenIfAvailable(context: Context): Boolean {
            return serviceInstance?.let { service ->
                if (getBooleanSetting(context, context.getString(R.string.DoubleTapLockScreen), false)) {
                    service.lockScreen()
                    true
                } else {
                    false
                }
            } ?: false
        }

        private const val TAG = "DoubleTapLockService"

        /**
         * Check if the accessibility service is enabled
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val accessibilityEnabled = try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Settings.SettingNotFoundException) {
                0
            }

            if (accessibilityEnabled == 1) {
                val services = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                val serviceName = "${context.packageName}/${DoubleTapLockAccessibilityService::class.java.name}"
                return services?.contains(serviceName) == true
            }
            return false
        }

        /**
         * Open accessibility settings for the user to enable the service
         */
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInstance = this
        Log.d(TAG, "Minimal accessibility service connected - screen lock only")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Minimal implementation - we don't process accessibility events
        // This service is only used for screen locking capability
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceInstance = null
        Log.d(TAG, "Accessibility service destroyed")
    }

    /**
     * Lock the screen using device admin or accessibility service
     */
    private fun lockScreen() {
        try {
            // Try to lock screen using accessibility service global action
            val success = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            if (success) {
                Log.d(TAG, "Screen locked successfully using accessibility service")
            } else {
                Log.w(TAG, "Failed to lock screen using accessibility service")
                // Fallback: try using device policy manager if available
                lockScreenFallback()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error locking screen", e)
            lockScreenFallback()
        }
    }

    /**
     * Fallback method to lock screen
     */
    private fun lockScreenFallback() {
        try {
            // Try to use device policy manager as fallback
            val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()
            Log.d(TAG, "Screen locked using device policy manager fallback")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock screen with fallback method", e)
        }
    }


}
