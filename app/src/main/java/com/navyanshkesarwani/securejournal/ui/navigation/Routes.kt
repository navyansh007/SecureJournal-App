package com.navyanshkesarwani.securejournal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val ON_THIS_DAY = "on_this_day"
    const val SETTINGS = "settings"
    const val ENTRY = "entry/{id}"
    const val EDITOR = "editor?id={id}"
    const val CLOUD = "cloud"
    const val CHANGE_PIN = "change_pin"

    fun entry(id: Long) = "entry/$id"
    fun editor(id: Long? = null) = if (id == null) "editor" else "editor?id=$id"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Journal", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH(Routes.SEARCH, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    ON_THIS_DAY(Routes.ON_THIS_DAY, "On this day", Icons.Filled.History, Icons.Outlined.History),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}
