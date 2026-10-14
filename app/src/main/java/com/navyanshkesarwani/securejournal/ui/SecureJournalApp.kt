package com.navyanshkesarwani.securejournal.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.ui.navigation.AppNavHost
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.screens.auth.BiometricSetupScreen
import com.navyanshkesarwani.securejournal.ui.screens.auth.LockScreen
import com.navyanshkesarwani.securejournal.ui.screens.auth.OnboardingScreen
import com.navyanshkesarwani.securejournal.ui.screens.auth.PinSetupScreen

private enum class SetupStep { WELCOME, PIN, BIOMETRIC }

/** Root: first-run setup, then the lock gate, then the journal itself. */
@Composable
fun SecureJournalApp() {
    when {
        SettingsStore.pin == null -> SetupFlow()
        else -> Box(Modifier.fillMaxSize()) {
            val locked = !Session.unlocked
            // While locked, hide the journal from accessibility services (screen readers) as well.
            Box(if (locked) Modifier.clearAndSetSemantics {} else Modifier) {
                // Wait for the first read from the database so screens never show a half-loaded journal.
                if (JournalRepository.isLoaded) MainContent()
            }
            // The lock screen sits on top so navigation state survives a re-lock.
            if (locked) LockScreen()
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
                SettingsStore.pin = newPin
                Session.unlocked = true
            })
        }
    }
}

@Composable
private fun MainContent() {
    AppNavHost()
}
