package com.navyanshkesarwani.securejournal

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.navyanshkesarwani.securejournal.data.AutoLock
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.SecureJournalApp
import com.navyanshkesarwani.securejournal.ui.theme.SecureJournalTheme
import com.navyanshkesarwani.securejournal.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private var backgroundedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = when (SettingsStore.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Keep status/navigation bar icons readable when the in-app theme differs from the system one.
            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
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
