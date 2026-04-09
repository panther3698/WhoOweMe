package com.example.whoowesme.util

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

object AppLockManager {
    fun isAuthAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            manager.canAuthenticate()
        }
        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            return true
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isDeviceSecure
        }

        return false
    }

    fun buildPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Unlock Who Owes Me")
            setSubtitle("Use your biometric or device lock to continue")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
            } else {
                setDeviceCredentialAllowed(true)
            }
        }.build()
    }
}
