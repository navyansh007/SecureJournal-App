package com.navyanshkesarwani.securejournal.ui.screens

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.data.AutoLock
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.SampleData
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.components.SectionHeader
import com.navyanshkesarwani.securejournal.reminder.Reminder
import com.navyanshkesarwani.securejournal.ui.components.canUseBiometrics
import com.navyanshkesarwani.securejournal.ui.components.showBiometricPrompt
import com.navyanshkesarwani.securejournal.ui.components.TimePickerDialog
import com.navyanshkesarwani.securejournal.ui.formatDateTime
import com.navyanshkesarwani.securejournal.ui.formatTime
import com.navyanshkesarwani.securejournal.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onChangePin: () -> Unit, onOpenBackup: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAutoLockMenu by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }

    fun showMessage(text: String) {
        scope.launch { snackbar.showSnackbar(text) }
    }

    fun enableBiometrics() {
        if (!canUseBiometrics(context)) {
            showMessage("No fingerprint or face is set up on this phone")
            return
        }
        showBiometricPrompt(
            context,
            title = "Enable biometric unlock",
            subtitle = "Confirm your fingerprint",
            negativeText = "Cancel",
            onSuccess = { SettingsStore.biometricEnabled = true }
        )
    }

    fun setReminder(enabled: Boolean) {
        SettingsStore.reminderEnabled = enabled
        Reminder.update(context)
        if (enabled) showMessage("Reminder set for ${SettingsStore.reminderTime.formatTime()} every day")
    }

    // Android 13+ asks the user before an app may post notifications.
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) setReminder(true) else showMessage("Allow notifications to get the daily reminder")
    }

    fun turnOnReminder() {
        if (Reminder.hasPermission(context)) setReminder(true)
        else notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Security")
            SettingsRow(Icons.Outlined.Password, "Change PIN", "Update your 4-digit unlock PIN", onClick = onChangePin)
            SettingsRow(
                Icons.Outlined.Fingerprint, "Biometric unlock", "Use fingerprint or face to unlock",
                onClick = { if (SettingsStore.biometricEnabled) SettingsStore.biometricEnabled = false else enableBiometrics() }
            ) {
                Switch(
                    checked = SettingsStore.biometricEnabled,
                    onCheckedChange = { if (it) enableBiometrics() else SettingsStore.biometricEnabled = false }
                )
            }
            Box {
                SettingsRow(
                    Icons.Outlined.LockClock, "Auto-lock", SettingsStore.autoLock.label,
                    onClick = { showAutoLockMenu = true }
                )
                DropdownMenu(
                    expanded = showAutoLockMenu,
                    onDismissRequest = { showAutoLockMenu = false },
                    modifier = Modifier.padding(start = 56.dp)
                ) {
                    AutoLock.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { SettingsStore.autoLock = option; showAutoLockMenu = false }
                        )
                    }
                }
            }
            SettingsRow(Icons.Outlined.Lock, "Lock now", "Return to the lock screen", onClick = { Session.lock() })
            SettingsRow(
                Icons.Outlined.VisibilityOff, "Hide in recent apps", "Blank the app preview and block screenshots",
                onClick = { SettingsStore.hideInRecents = !SettingsStore.hideInRecents }
            ) {
                Switch(checked = SettingsStore.hideInRecents, onCheckedChange = { SettingsStore.hideInRecents = it })
            }

            SectionHeader("Reminders", Modifier.padding(top = 16.dp))
            SettingsRow(
                Icons.Outlined.Notifications, "Daily reminder", "A gentle nudge to write every day",
                onClick = { if (SettingsStore.reminderEnabled) setReminder(false) else turnOnReminder() }
            ) {
                Switch(
                    checked = SettingsStore.reminderEnabled,
                    onCheckedChange = { if (it) turnOnReminder() else setReminder(false) }
                )
            }
            SettingsRow(
                Icons.Outlined.Schedule, "Reminder time", SettingsStore.reminderTime.formatTime(),
                enabled = SettingsStore.reminderEnabled,
                onClick = { showTimePicker = true }
            )
            SettingsRow(
                Icons.Outlined.NotificationsActive, "Send test reminder", "Show the reminder notification now",
                enabled = SettingsStore.reminderEnabled,
                onClick = { Reminder.showNotification(context) }
            )

            SectionHeader("Appearance", Modifier.padding(top = 16.dp))
            Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Palette, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(20.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = SettingsStore.themeMode == mode,
                            onClick = { SettingsStore.themeMode = mode },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size)
                        ) { Text(mode.label) }
                    }
                }
            }

            SectionHeader("Backup & data", Modifier.padding(top = 16.dp))
            SettingsRow(
                Icons.Outlined.Backup, "Backup & restore",
                SettingsStore.lastBackup?.let { "Last backup ${it.formatDateTime()}" } ?: "Save your journal to a file or Google Drive",
                onClick = onOpenBackup
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
            SettingsRow(
                Icons.Outlined.IosShare, "Export entries", "Share all entries as plain text",
                onClick = {
                    val text = JournalRepository.entries.joinToString("\n\n— — —\n\n") { it.asShareText() }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "SecureJournal export")
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    Session.leavingForSystemScreen = true
                    context.startActivity(Intent.createChooser(send, "Export journal"))
                }
            )
            SettingsRow(
                Icons.Outlined.AutoAwesome, "Load sample entries", "Add ${SampleData.entries().size} example entries to explore the app",
                onClick = {
                    scope.launch {
                        val added = JournalRepository.addAll(SampleData.entries())
                        snackbar.showSnackbar(if (added == 0) "Sample entries are already in your journal" else "Added $added sample entries")
                    }
                }
            )
            SettingsRow(
                Icons.Outlined.DeleteForever, "Delete all entries", "Permanently erase your journal from this device",
                tint = MaterialTheme.colorScheme.error,
                onClick = { confirmWipe = true }
            )

            SectionHeader("About", Modifier.padding(top = 16.dp))
            val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            SettingsRow(Icons.Outlined.Info, "SecureJournal", "Version $version", onClick = {})
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(SettingsStore.reminderTime.hour, SettingsStore.reminderTime.minute)
        TimePickerDialog(
            title = "Daily reminder",
            onDismiss = { showTimePicker = false },
            onConfirm = {
                SettingsStore.reminderTime = LocalTime.of(state.hour, state.minute)
                showTimePicker = false
                setReminder(true)
            }
        ) { TimePicker(state) }
    }

    if (confirmWipe) {
        AlertDialog(
            onDismissRequest = { confirmWipe = false },
            icon = { Icon(Icons.Outlined.DeleteForever, null) },
            title = { Text("Delete all entries?") },
            text = { Text("All ${JournalRepository.entries.size} entries will be erased from this device. Backup files you saved are not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    JournalRepository.deleteAll()
                    confirmWipe = false
                    scope.launch { snackbar.showSnackbar("All entries deleted") }
                }) { Text("Delete all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmWipe = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val alpha = if (enabled) 1f else 0.4f
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else tint
        Icon(icon, null, tint = color.copy(alpha = alpha))
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = (if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else tint).copy(alpha = alpha)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}
