package com.navyanshkesarwani.securejournal.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.formatDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Saves the whole journal to a JSON file, and reads it back.
 * The Android file picker (Storage Access Framework) lets the user choose where the file goes:
 * Downloads, an SD card, or Google Drive for a cloud copy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var busyText by remember { mutableStateOf<String?>(null) }

    fun writeBackup(uri: Uri) {
        scope.launch {
            busyText = "Saving backup…"
            val json = JournalRepository.toJson()
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(json.toByteArray()) }
                }.isSuccess
            }
            busyText = null
            if (ok) {
                SettingsStore.lastBackup = LocalDateTime.now()
                snackbar.showSnackbar("Backup saved · ${JournalRepository.entries.size} entries")
            } else {
                snackbar.showSnackbar("Couldn't save the backup file")
            }
        }
    }

    fun readBackup(uri: Uri) {
        scope.launch {
            busyText = "Restoring…"
            val restored = withContext(Dispatchers.IO) {
                runCatching {
                    val json = context.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
                    JournalRepository.fromJson(json)
                }.getOrNull()
            }
            val added = restored?.let { JournalRepository.addAll(it) }
            busyText = null
            snackbar.showSnackbar(
                when {
                    added == null -> "This file is not a SecureJournal backup"
                    added == 0 -> "Nothing new · your journal already has these entries"
                    else -> "Restored $added entr${if (added == 1) "y" else "ies"}"
                }
            )
        }
    }

    // System "Save as" and "Open" screens. The result is a content Uri we can write to / read from.
    val createFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) writeBackup(uri)
    }
    val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) readBackup(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & restore") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Backup, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(20.dp))
            Text("Keep a copy of your journal", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Save all entries to a file. Pick Google Drive in the file picker for a cloud copy, " +
                    "then restore it on this or a new phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Column(Modifier.fillMaxWidth()) {
                Text("${JournalRepository.entries.size} entries on this phone", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Last backup: ${SettingsStore.lastBackup?.formatDateTime() ?: "Never"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))

            busyText?.let {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
            }
            Button(
                onClick = {
                    Session.leavingForSystemScreen = true
                    createFile.launch("SecureJournal-backup-${LocalDate.now()}.json")
                },
                enabled = busyText == null && JournalRepository.entries.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Back up now") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    Session.leavingForSystemScreen = true
                    openFile.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
                },
                enabled = busyText == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Restore from backup") }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Info, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(10.dp))
                Text(
                    "The backup file is plain text that anyone with the file can read, so keep it somewhere private. " +
                        "Restoring adds entries from the file and skips ones you already have.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
