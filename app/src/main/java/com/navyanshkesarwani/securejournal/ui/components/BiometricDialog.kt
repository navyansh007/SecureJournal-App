package com.navyanshkesarwani.securejournal.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Mimics the system BiometricPrompt. Tapping the sensor icon simulates a successful scan
 * so the demo flow can be walked through on any device or emulator.
 */
@Composable
fun SimulatedBiometricDialog(
    title: String = "Unlock SecureJournal",
    subtitle: String = "Touch the fingerprint sensor",
    dismissLabel: String = "Use PIN",
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    var scanning by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }
    LaunchedEffect(scanning) {
        if (!scanning) return@LaunchedEffect
        delay(900)
        verified = true
        delay(500)
        onSuccess()
    }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "scale"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Box(
                    Modifier
                        .size(84.dp)
                        .scale(if (scanning && !verified) pulse else 1f)
                        .clip(CircleShape)
                        .clickable(enabled = !scanning) { scanning = true }
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (verified) Icons.Filled.CheckCircle else Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    when {
                        verified -> "Verified"
                        scanning -> "Scanning…"
                        else -> "Tap the sensor to simulate a scan"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } }
    )
}
