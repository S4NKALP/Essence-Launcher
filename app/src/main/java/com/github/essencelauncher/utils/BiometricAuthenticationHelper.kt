/*
 * Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information.
 */
 
package com.github.essencelauncher.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.essencelauncher.R

/**
 * Helper class for handling biometric authentication
 * Uses Android's BiometricPrompt API for secure authentication
 * 
 * @author Essence Launcher Team
 */
class BiometricAuthenticationHelper(private val activity: FragmentActivity) {

    /**
     * Callback interface for authentication results
     */
    interface AuthenticationCallback {
        fun onAuthenticationSucceeded()
        fun onAuthenticationFailed()
        fun onAuthenticationError(errorCode: Int, errorMessage: String)
    }

    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Show biometric authentication prompt
     * 
     * @param title The title to show in the authentication dialog
     * @param subtitle The subtitle to show in the authentication dialog
     * @param callback Callback to handle authentication results
     */
    fun authenticate(
        title: String,
        subtitle: String,
        callback: AuthenticationCallback
    ) {
        if (!isBiometricAvailable()) {
            callback.onAuthenticationError(
                BiometricPrompt.ERROR_HW_UNAVAILABLE,
                activity.getString(R.string.biometric_not_available)
            )
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.onAuthenticationError(errorCode, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.onAuthenticationSucceeded()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Convenience method to authenticate for opening a locked app
     */
    fun authenticateForApp(
        appName: String,
        callback: AuthenticationCallback
    ) {
        val title = activity.getString(R.string.authentication_required)
        val subtitle = activity.getString(R.string.authenticate_to_open, appName)
        authenticate(title, subtitle, callback)
    }

    /**
     * Convenience method to authenticate for unlocking an app
     */
    fun authenticateForUnlock(
        appName: String,
        callback: AuthenticationCallback
    ) {
        val title = activity.getString(R.string.authentication_required)
        val subtitle = activity.getString(R.string.authenticate_to_unlock, appName)
        authenticate(title, subtitle, callback)
    }

    /**
     * Convenience method to authenticate for accessing hidden apps
     */
    fun authenticateForHiddenApps(
        callback: AuthenticationCallback
    ) {
        val title = activity.getString(R.string.authentication_required)
        val subtitle = activity.getString(R.string.authenticate_to_access_hidden_apps)
        authenticate(title, subtitle, callback)
    }
}
