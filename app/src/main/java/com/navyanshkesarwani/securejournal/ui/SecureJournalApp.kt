package com.navyanshkesarwani.securejournal.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.screens.auth.BiometricSetupScreen
import com.navyanshkesarwani.securejournal.ui.screens.auth.LockScreen
import com.navyanshkesarwani.securejournal.ui.screens.auth.OnboardingScreen
import com.navyanshkesarwani.securejournal.ui.screens.auth.PinSetupScreen

private enum class SetupStep { WELCOME, PIN, BIOMETRIC }

/** Root: first-run setup, then the lock gate, then the journal itself. */
@Composable
fun SecureJournalApp() {
    val pin = SettingsStore.pin
    when {
        pin == null -> SetupFlow()
        else -> Box(Modifier.fillMaxSize()) {
            MainContent()
            // The lock screen sits on top so navigation state survives a re-lock.
            if (!Session.unlocked) {
                LockScreen(
                    correctPin = pin,
                    biometricEnabled = SettingsStore.biometricEnabled,
                    onUnlocked = { Session.unlocked = true }
                )
            }
        }
    }
}

@Composable
private fun SetupFlow() {
    var step by rememberSaveable { mutableStateOf(SetupStep.WELCOME) }
    var newPin by rememberSaveable { mutableStateOf("") }
    AnimatedContent(step, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "setup") { current ->
        when (current) {
            SetupStep.WELCOME -> OnboardingScreen(onGetStarted = { step = SetupStep.PIN })
            SetupStep.PIN -> PinSetupScreen(
                onPinCreated = { newPin = it; step = SetupStep.BIOMETRIC },
                onBack = { step = SetupStep.WELCOME }
            )
            SetupStep.BIOMETRIC -> BiometricSetupScreen(onDone = { enabled ->
                SettingsStore.biometricEnabled = enabled
                Session.unlocked = true
                SettingsStore.pin = newPin
            })
        }
    }
}

@Composable
private fun MainContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Journal") }
}
