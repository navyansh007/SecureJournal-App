package com.navyanshkesarwani.securejournal.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.navyanshkesarwani.securejournal.ui.theme.ThemeMode
import java.time.LocalDateTime
import java.time.LocalTime

enum class AutoLock(val label: String) {
    IMMEDIATELY("Immediately"),
    ONE_MINUTE("After 1 minute"),
    FIVE_MINUTES("After 5 minutes"),
    NEVER("Never"),
}

/** In-memory stand-in for SharedPreferences / EncryptedSharedPreferences. */
object SettingsStore {
    var pin by mutableStateOf<String?>(null)
    var biometricEnabled by mutableStateOf(false)
    var autoLock by mutableStateOf(AutoLock.IMMEDIATELY)
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    var reminderEnabled by mutableStateOf(true)
    var reminderTime by mutableStateOf(LocalTime.of(21, 0))

    var cloudEmail by mutableStateOf<String?>(null)
    var autoSync by mutableStateOf(true)
    var lastBackup by mutableStateOf<LocalDateTime?>(null)
}

/** Whether the journal is currently unlocked for this session. */
object Session {
    var unlocked by mutableStateOf(false)

    fun lock() {
        if (SettingsStore.pin != null) unlocked = false
    }
}
