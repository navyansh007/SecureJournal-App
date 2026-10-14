package com.navyanshkesarwani.securejournal.ui.components

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** True when the phone has a fingerprint/face sensor and the user has enrolled at least one. */
fun canUseBiometrics(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Shows the real Android fingerprint/face dialog (BiometricPrompt).
 * [onSuccess] runs only when the system confirms the user's biometric.
 */
fun showBiometricPrompt(
    context: Context,
    title: String = "Unlock SecureJournal",
    subtitle: String = "Confirm it's you",
    negativeText: String = "Use PIN",
    onSuccess: () -> Unit,
) {
    val activity = context.findActivity() ?: return
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
    }
    val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeText)
        .setAllowedAuthenticators(BIOMETRIC_WEAK)
        .build()
    prompt.authenticate(info)
}

/** In Compose, LocalContext may wrap the activity, so walk up until we find it. */
private fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}
