package com.navyanshkesarwani.securejournal.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.components.PIN_LENGTH
import com.navyanshkesarwani.securejournal.ui.components.PinDots
import com.navyanshkesarwani.securejournal.ui.components.PinPad
import com.navyanshkesarwani.securejournal.ui.components.canUseBiometrics
import com.navyanshkesarwani.securejournal.ui.components.showBiometricPrompt
import kotlinx.coroutines.delay

@Composable
fun LockScreen() {
    val context = LocalContext.current
    val biometricAvailable = SettingsStore.biometricEnabled && canUseBiometrics(context)
    var input by remember { mutableStateOf("") }
    var errorTrigger by remember { mutableIntStateOf(0) }
    var showError by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val secondsLeft = ((SettingsStore.lockedUntil - now + 999) / 1000).coerceAtLeast(0)
    val blocked = secondsLeft > 0

    // Open the fingerprint dialog straight away when the lock screen appears.
    LaunchedEffect(Unit) {
        if (biometricAvailable && !blocked) showBiometricPrompt(context) { Session.unlockWithBiometrics() }
    }

    // Tick once a second while the PIN pad is blocked so the countdown updates.
    LaunchedEffect(blocked) {
        while (SettingsStore.lockedUntil > System.currentTimeMillis()) {
            delay(1000)
            now = System.currentTimeMillis()
        }
        now = System.currentTimeMillis()
    }

    LaunchedEffect(input) {
        if (input.length < PIN_LENGTH) return@LaunchedEffect
        delay(150)
        if (!Session.tryUnlock(input)) {
            errorTrigger++
            showError = true
            input = ""
            now = System.currentTimeMillis()
        }
    }

    val attempts = SettingsStore.failedAttempts
    val message = when {
        blocked -> "Too many wrong attempts. Try again in $secondsLeft s"
        showError -> "Wrong PIN · $attempts failed attempt${if (attempts > 1) "s" else ""}"
        else -> "Enter your PIN to unlock"
    }

    Column(
        Modifier
            .fillMaxSize()
            // Swallow every tap so it can never reach the journal drawn underneath.
            .clickable(interactionSource = null, indication = null) {}
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Lock, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(24.dp))
            Text("SecureJournal", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (showError || blocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            PinDots(input.length, errorTrigger, isError = showError && input.isEmpty())
        }
        PinPad(
            onDigit = {
                if (!blocked && input.length < PIN_LENGTH) input += it
                showError = false
            },
            onBackspace = { input = input.dropLast(1) },
            onBiometric = if (biometricAvailable) ({
                showBiometricPrompt(context) { Session.unlockWithBiometrics() }
            }) else null,
        )
        Spacer(Modifier.height(24.dp))
    }
}
