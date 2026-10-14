package com.navyanshkesarwani.securejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.SampleData
import com.navyanshkesarwani.securejournal.ui.components.EmptyState
import com.navyanshkesarwani.securejournal.ui.components.EntryCard
import com.navyanshkesarwani.securejournal.ui.components.SectionHeader
import com.navyanshkesarwani.securejournal.ui.formatLongDay
import com.navyanshkesarwani.securejournal.ui.formatMonth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    onOpenOnThisDay: () -> Unit,
    onLock: () -> Unit,
) {
    val entries = JournalRepository.entries
    val memories = JournalRepository.onThisDay()
    val today = LocalDate.now()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(greeting(), style = MaterialTheme.typography.titleLarge)
                        Text(
                            today.formatLongDay(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLock) { Icon(Icons.Outlined.Lock, "Lock journal") }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEntry,
                icon = { Icon(Icons.Filled.Edit, null) },
                text = { Text("Write") }
            )
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(
                Icons.Outlined.EditNote,
                "Your journal is empty",
                "Tap Write to capture your first thought. Entries are saved privately on this device.",
                Modifier.padding(padding),
                action = {
                    // Optional demo content, saved to the database like any other entry.
                    OutlinedButton(onClick = { scope.launch { JournalRepository.addAll(SampleData.entries()) } }) {
                        Text("Load sample entries")
                    }
                }
            )
            return@Scaffold
        }

        val byMonth = entries.groupBy { it.createdAt.toLocalDate().withDayOfMonth(1) }
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                StatsRow(
                    streak = JournalRepository.currentStreak(),
                    total = entries.size,
                    wroteToday = entries.any { it.createdAt.toLocalDate() == today }
                )
            }
            if (memories.isNotEmpty()) {
                item {
                    val (label, list) = memories.first()
                    OnThisDayTeaser(label, list.first().title, memories.sumOf { it.second.size }, onOpenOnThisDay)
                }
            }
            byMonth.forEach { (month, monthEntries) ->
                item(key = "header-$month") { SectionHeader(month.formatMonth(), Modifier.padding(top = 8.dp)) }
                items(monthEntries, key = { it.id }) { entry ->
                    EntryCard(entry, onClick = { onOpenEntry(entry.id) }, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun StatsRow(streak: Int, total: Int, wroteToday: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard("🔥", "$streak day${if (streak == 1) "" else "s"}", "Streak", Modifier.weight(1f))
        StatCard("📓", "$total", "Entries", Modifier.weight(1f))
        StatCard(if (wroteToday) "✅" else "✍️", if (wroteToday) "Done" else "Pending", "Today", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OnThisDayTeaser(label: String, title: String, count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("On this day · $label", style = MaterialTheme.typography.labelMedium)
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (count > 1) {
                    Text("+${count - 1} more memor${if (count == 2) "y" else "ies"}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
        }
    }
}
