package com.navyanshkesarwani.securejournal

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.navyanshkesarwani.securejournal.data.AutoLock
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.data.SettingsStore
import com.navyanshkesarwani.securejournal.ui.SecureJournalApp
import com.navyanshkesarwani.securejournal.ui.theme.SecureJournalTheme
import com.navyanshkesarwani.securejournal.ui.theme.ThemeMode

/** FragmentActivity (not ComponentActivity) because the system BiometricPrompt needs it. */
class MainActivity : FragmentActivity() {

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
            // FLAG_SECURE hides the journal in the recent-apps screen and blocks screenshots.
            SideEffect {
                if (SettingsStore.hideInRecents) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
        // Coming back from our own share sheet or file picker is not "leaving the app".
        if (Session.leavingForSystemScreen) {
            Session.leavingForSystemScreen = false
            return
        }
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
