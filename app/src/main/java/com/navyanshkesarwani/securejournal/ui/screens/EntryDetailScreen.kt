package com.navyanshkesarwani.securejournal.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.data.JournalEntry
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.ui.components.MoodBadge
import com.navyanshkesarwani.securejournal.ui.formatDateTime
import com.navyanshkesarwani.securejournal.ui.formatLongDay
import com.navyanshkesarwani.securejournal.ui.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(entryId: Long, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val entry = JournalRepository.entries.firstOrNull { it.id == entryId }
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(entry) { if (entry == null) onBack() }
    if (entry == null) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, entry.title)
                            putExtra(Intent.EXTRA_TEXT, entry.asShareText())
                        }
                        context.startActivity(Intent.createChooser(send, "Share entry"))
                    }) { Icon(Icons.Outlined.Share, "Share") }
                    IconButton(onClick = { onEdit(entry.id) }) { Icon(Icons.Outlined.Edit, "Edit") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Delete") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoodBadge(entry.mood, size = 48)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Feeling ${entry.mood.label.lowercase()}", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${entry.createdAt.toLocalDate().formatLongDay()} · ${entry.createdAt.formatTime()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(entry.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text(entry.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            if (entry.updatedAt != entry.createdAt) {
                Text(
                    "Last edited ${entry.updatedAt.formatDateTime()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Outlined.Delete, null) },
            title = { Text("Delete this entry?") },
            text = { Text("\"${entry.title.ifBlank { "Untitled" }}\" will be permanently removed. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    JournalRepository.delete(entry.id)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

fun JournalEntry.asShareText(): String =
    "${mood.emoji} $title\n${createdAt.formatDateTime()}\n\n$content"
