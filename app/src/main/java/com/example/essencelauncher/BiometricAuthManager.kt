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
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BiometricAuthManager(private val fragment: Fragment) {

    interface AuthCallback {
        fun onAuthSuccess()
        fun onAuthError(errorMessage: String)
        fun onAuthFailed()
    }

    fun authenticate(appName: String, callback: AuthCallback) {
        authenticate(appName, "launch", callback)
    }

    fun authenticate(appName: String, action: String, callback: AuthCallback) {
        val context = fragment.requireContext()
        val biometricManager = BiometricManager.from(context)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(appName, action, callback)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                callback.onAuthError("No biometric hardware available")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                callback.onAuthError("Biometric hardware unavailable")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                callback.onAuthError("No biometric credentials enrolled")
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                callback.onAuthError("Security update required")
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                callback.onAuthError("Biometric authentication not supported")
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                callback.onAuthError("Biometric status unknown")
            }
        }
    }

    private fun showBiometricPrompt(appName: String, action: String, callback: AuthCallback) {
        val executor = ContextCompat.getMainExecutor(fragment.requireContext())
        val biometricPrompt = BiometricPrompt(fragment, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback.onAuthError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callback.onAuthSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callback.onAuthFailed()
            }
        })

        val title = when (action) {
            "unlock" -> "Unlock $appName"
            "access" -> "Access $appName"
            else -> "Launch $appName"
        }

        val subtitle = when (action) {
            "unlock" -> "Use your biometric credential or device password to unlock this app"
            "access" -> "Use your biometric credential or device password to access $appName"
            else -> "Use your biometric credential or device password to launch this app"
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        fun isAuthenticationAvailable(context: Context): Boolean {
            val biometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}
