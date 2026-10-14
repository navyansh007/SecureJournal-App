package com.navyanshkesarwani.securejournal.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.navyanshkesarwani.securejournal.ui.theme.ThemeMode
import java.time.LocalDateTime
import java.time.LocalTime

enum class AutoLock(val label: String) {
    IMMEDIATELY("Immediately"),
    ONE_MINUTE("After 1 minute"),
    FIVE_MINUTES("After 5 minutes"),
    NEVER("Never"),
}

/**
 * App settings saved in SharedPreferences (private to this app).
 * Each property is also Compose state, so the UI updates as soon as a setting changes,
 * and each setter writes the new value to disk straight away.
 */
object SettingsStore {

    private lateinit var prefs: SharedPreferences

    private val _pin = mutableStateOf<String?>(null)
    private val _biometricEnabled = mutableStateOf(false)
    private val _autoLock = mutableStateOf(AutoLock.IMMEDIATELY)
    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
    private val _hideInRecents = mutableStateOf(true)
    private val _reminderEnabled = mutableStateOf(false)
    private val _reminderTime = mutableStateOf(LocalTime.of(21, 0))
    private val _lastBackup = mutableStateOf<LocalDateTime?>(null)

    /** Called once from [com.navyanshkesarwani.securejournal.SecureJournalApplication]. */
    fun init(context: Context) {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        _pin.value = prefs.getString("pin", null)
        _biometricEnabled.value = prefs.getBoolean("biometric", false)
        _autoLock.value = AutoLock.valueOf(prefs.getString("auto_lock", AutoLock.IMMEDIATELY.name)!!)
        _themeMode.value = ThemeMode.valueOf(prefs.getString("theme", ThemeMode.SYSTEM.name)!!)
        _hideInRecents.value = prefs.getBoolean("hide_in_recents", true)
        _reminderEnabled.value = prefs.getBoolean("reminder", false)
        _reminderTime.value = LocalTime.parse(prefs.getString("reminder_time", "21:00"))
        _lastBackup.value = prefs.getString("last_backup", null)?.let(LocalDateTime::parse)
    }

    /** null until the user creates a PIN on first launch. */
    var pin: String?
        get() = _pin.value
        set(value) { _pin.value = value; prefs.edit { putString("pin", value) } }

    var biometricEnabled: Boolean
        get() = _biometricEnabled.value
        set(value) { _biometricEnabled.value = value; prefs.edit { putBoolean("biometric", value) } }

    var autoLock: AutoLock
        get() = _autoLock.value
        set(value) { _autoLock.value = value; prefs.edit { putString("auto_lock", value.name) } }

    var themeMode: ThemeMode
        get() = _themeMode.value
        set(value) { _themeMode.value = value; prefs.edit { putString("theme", value.name) } }

    /** Hides the journal in the recent-apps screen and blocks screenshots (FLAG_SECURE). */
    var hideInRecents: Boolean
        get() = _hideInRecents.value
        set(value) { _hideInRecents.value = value; prefs.edit { putBoolean("hide_in_recents", value) } }

    var reminderEnabled: Boolean
        get() = _reminderEnabled.value
        set(value) { _reminderEnabled.value = value; prefs.edit { putBoolean("reminder", value) } }

    var reminderTime: LocalTime
        get() = _reminderTime.value
        set(value) { _reminderTime.value = value; prefs.edit { putString("reminder_time", value.toString()) } }

    var lastBackup: LocalDateTime?
        get() = _lastBackup.value
        set(value) { _lastBackup.value = value; prefs.edit { putString("last_backup", value?.toString()) } }

    /** Wrong PIN attempts in a row. Saved so closing the app doesn't reset the count. */
    var failedAttempts: Int
        get() = prefs.getInt("failed_attempts", 0)
        set(value) = prefs.edit { putInt("failed_attempts", value) }

    /** Time (in millis) until which the PIN pad is blocked after too many wrong attempts. */
    var lockedUntil: Long
        get() = prefs.getLong("locked_until", 0L)
        set(value) = prefs.edit { putLong("locked_until", value) }
}

/** Whether the journal is currently unlocked. Always starts locked when the app process starts. */
object Session {
    private const val MAX_ATTEMPTS = 5
    private const val LOCKOUT_MILLIS = 30_000L

    var unlocked by mutableStateOf(false)

    /** Set before opening the share sheet or file picker, so coming back from it doesn't lock the app. */
    var leavingForSystemScreen = false

    fun lock() {
        if (SettingsStore.pin != null) unlocked = false
    }

    /** Checks [input] against the saved PIN and blocks the pad for 30 s after every 5 wrong tries. */
    fun tryUnlock(input: String): Boolean {
        if (input == SettingsStore.pin) {
            SettingsStore.failedAttempts = 0
            unlocked = true
            return true
        }
        SettingsStore.failedAttempts += 1
        if (SettingsStore.failedAttempts % MAX_ATTEMPTS == 0) {
            SettingsStore.lockedUntil = System.currentTimeMillis() + LOCKOUT_MILLIS
        }
        return false
    }

    fun unlockWithBiometrics() {
        SettingsStore.failedAttempts = 0
        unlocked = true
    }
}
