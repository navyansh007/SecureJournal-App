package com.navyanshkesarwani.securejournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.navyanshkesarwani.securejournal.data.AutoLock
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.SecureJournalApp
import com.navyanshkesarwani.securejournal.ui.theme.SecureJournalTheme

class MainActivity : ComponentActivity() {

    private var backgroundedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecureJournalTheme(themeMode = SettingsStore.themeMode) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    SecureJournalApp()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        backgroundedAt = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        if (backgroundedAt == 0L) return
        val away = System.currentTimeMillis() - backgroundedAt
        val shouldLock = when (SettingsStore.autoLock) {
            AutoLock.IMMEDIATELY -> true
            AutoLock.ONE_MINUTE -> away >= 60_000
            AutoLock.FIVE_MINUTES -> away >= 5 * 60_000
            AutoLock.NEVER -> false
        }
        if (shouldLock) Session.lock()
    }
}
