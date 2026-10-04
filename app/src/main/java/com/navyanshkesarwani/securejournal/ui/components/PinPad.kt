package com.navyanshkesarwani.securejournal.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

const val PIN_LENGTH = 4

/** Row of dots showing how many digits were entered. Shakes when [errorTrigger] changes. */
@Composable
fun PinDots(length: Int, errorTrigger: Int, isError: Boolean) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorTrigger) {
        if (errorTrigger == 0) return@LaunchedEffect
        for (offset in listOf(24f, -24f, 16f, -16f, 8f, 0f)) {
            shake.animateTo(offset, spring(stiffness = Spring.StiffnessHigh))
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
    ) {
        repeat(PIN_LENGTH) { i ->
            val filled = i < length
            val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (filled) color else MaterialTheme.colorScheme.surface)
                    .border(2.dp, color, CircleShape)
            )
        }
    }
}

@Composable
fun PinPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: (() -> Unit)? = null,
) {
    val rows = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                row.forEach { digit -> PinKey(onClick = { onDigit(digit) }) { Text("$digit", fontSize = 28.sp) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            if (onBiometric != null) {
                PinKey(onClick = onBiometric, filled = false) {
                    Icon(Icons.Outlined.Fingerprint, "Use fingerprint", Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Box(Modifier.size(76.dp))
            }
            PinKey(onClick = { onDigit('0') }) { Text("0", fontSize = 28.sp) }
            PinKey(onClick = onBackspace, filled = false) {
                Icon(Icons.AutoMirrored.Outlined.Backspace, "Delete")
            }
        }
    }
}

@Composable
private fun PinKey(onClick: () -> Unit, filled: Boolean = true, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (filled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}
