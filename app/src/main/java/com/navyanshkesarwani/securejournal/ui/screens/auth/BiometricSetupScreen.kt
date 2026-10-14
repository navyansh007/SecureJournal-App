package com.navyanshkesarwani.securejournal.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.ui.components.canUseBiometrics
import com.navyanshkesarwani.securejournal.ui.components.showBiometricPrompt

@Composable
fun BiometricSetupScreen(onDone: (enabled: Boolean) -> Unit) {
    val context = LocalContext.current
    val available = canUseBiometrics(context)

    Column(
        Modifier
            .fillMaxSize()
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
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Fingerprint, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(32.dp))
            Text("Unlock with fingerprint?", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                if (available) "Use your fingerprint or face to open your journal quickly. Your PIN will always work as a backup."
                else "No fingerprint or face is set up on this phone. Add one in the phone's Settings, then turn this on from SecureJournal's Settings.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = {
                showBiometricPrompt(
                    context,
                    title = "Enable biometric unlock",
                    subtitle = "Confirm your fingerprint",
                    negativeText = "Cancel",
                    onSuccess = { onDone(true) }
                )
            },
            enabled = available,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) { Text("Enable biometrics") }
        TextButton(onClick = { onDone(false) }, modifier = Modifier.padding(top = 8.dp)) { Text("Not now") }
    }
}
