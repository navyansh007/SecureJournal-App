package com.navyanshkesarwani.securejournal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Plum,
    onPrimary = Color.White,
    primaryContainer = PlumContainer,
    onPrimaryContainer = Color(0xFF21184A),
    secondary = Sage,
    secondaryContainer = Color(0xFFD8EADF),
    tertiary = Apricot,
    tertiaryContainer = Color(0xFFFFDBCF),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperVariant,
    surfaceContainerLow = Color(0xFFF6F2EB),
    surfaceContainer = PaperVariant,
    surfaceContainerHigh = Color(0xFFEBE5DC),
)

private val DarkColorScheme = darkColorScheme(
    primary = PlumLight,
    onPrimary = Color(0xFF2C2159),
    primaryContainer = PlumContainerDark,
    onPrimaryContainer = PlumContainer,
    secondary = SageLight,
    secondaryContainer = Color(0xFF3A5546),
    tertiary = ApricotLight,
    tertiaryContainer = Color(0xFF6E3B27),
    background = Night,
    onBackground = Color(0xFFE7E3EC),
    surface = Night,
    onSurface = Color(0xFFE7E3EC),
    surfaceVariant = NightVariant,
    surfaceContainerLow = Color(0xFF1B1A21),
    surfaceContainer = NightVariant,
    surfaceContainerHigh = Color(0xFF2B2833),
)

enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

@Composable
fun SecureJournalTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
