package com.navyanshkesarwani.securejournal.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.navyanshkesarwani.securejournal.data.Session
import com.navyanshkesarwani.securejournal.ui.screens.EditorScreen
import com.navyanshkesarwani.securejournal.ui.screens.EntryDetailScreen
import com.navyanshkesarwani.securejournal.ui.screens.HomeScreen
import com.navyanshkesarwani.securejournal.ui.screens.OnThisDayScreen
import com.navyanshkesarwani.securejournal.ui.screens.SearchScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { dest ->
                        val selected = dest.route == currentRoute
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateTopLevel(dest.route) },
                            icon = { Icon(if (selected) dest.selectedIcon else dest.icon, null) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        },
        // Each screen draws its own top bar and handles the status bar inset.
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp)
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .consumeWindowInsets(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNewEntry = { navController.navigate(Routes.editor()) },
                    onOpenEntry = { navController.navigate(Routes.entry(it)) },
                    onOpenOnThisDay = { navController.navigateTopLevel(Routes.ON_THIS_DAY) },
                    onLock = { Session.lock() },
                )
            }
            composable(
                Routes.ENTRY,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                EntryDetailScreen(
                    entryId = entry.arguments!!.getLong("id"),
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editor(it)) }
                )
            }
            composable(
                Routes.EDITOR,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val id = entry.arguments!!.getLong("id").takeIf { it > 0 }
                EditorScreen(entryId = id, onClose = { navController.popBackStack() })
            }
            composable(Routes.SEARCH) { SearchScreen(onOpenEntry = { navController.navigate(Routes.entry(it)) }) }
            composable(Routes.ON_THIS_DAY) { OnThisDayScreen(onOpenEntry = { navController.navigate(Routes.entry(it)) }) }
            composable(Routes.SETTINGS) { Placeholder("Settings") }
        }
    }
}

@Composable
private fun Placeholder(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(name) }
}

fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
