package com.navyanshkesarwani.securejournal.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.ui.components.PIN_LENGTH
import com.navyanshkesarwani.securejournal.ui.components.PinDots
import com.navyanshkesarwani.securejournal.ui.components.PinPad
import kotlinx.coroutines.delay

/**
 * Two-step PIN creation (enter + confirm). When [currentPin] is given, the user must
 * first verify it — used for "Change PIN" in settings.
 */
@Composable
fun PinSetupScreen(
    onPinCreated: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    currentPin: String? = null,
) {
    var step by remember { mutableStateOf(if (currentPin != null) Step.VERIFY else Step.CREATE) }
    var input by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var errorTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(input) {
        if (input.length < PIN_LENGTH) return@LaunchedEffect
        delay(150)
        when (step) {
            Step.VERIFY -> if (input == currentPin) {
                step = Step.CREATE; error = null
            } else {
                error = "Incorrect PIN"; errorTrigger++
            }
            Step.CREATE -> {
                firstPin = input; step = Step.CONFIRM; error = null
            }
            Step.CONFIRM -> if (input == firstPin) {
                onPinCreated(input)
            } else {
                error = "PINs don't match. Try again."; errorTrigger++
                step = Step.CREATE
            }
        }
        input = ""
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.align(Alignment.Start)) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(step.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                error ?: step.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            PinDots(input.length, errorTrigger, isError = error != null && input.isEmpty())
        }
        PinPad(
            onDigit = { if (input.length < PIN_LENGTH) input += it },
            onBackspace = { input = input.dropLast(1) },
        )
        Spacer(Modifier.height(24.dp))
    }
}

private enum class Step(val title: String, val subtitle: String) {
    VERIFY("Enter current PIN", "Verify it's you before changing your PIN"),
    CREATE("Create a PIN", "You'll use this $PIN_LENGTH-digit PIN to unlock your journal"),
    CONFIRM("Confirm your PIN", "Enter the same PIN again"),
}
