package com.navyanshkesarwani.securejournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.ui.components.EmptyState
import com.navyanshkesarwani.securejournal.ui.components.EntryCard
import com.navyanshkesarwani.securejournal.ui.components.SectionHeader
import com.navyanshkesarwani.securejournal.ui.formatDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnThisDayScreen(onOpenEntry: (Long) -> Unit) {
    val today = LocalDate.now()
    val memories = JournalRepository.onThisDay(today)

    Scaffold(topBar = { TopAppBar(title = { Text("On this day") }) }) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(Modifier.padding(20.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                today.format(DateTimeFormatter.ofPattern("MMMM")).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "${today.dayOfMonth}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (memories.isEmpty()) "No memories yet for today"
                                else "${memories.sumOf { it.second.size }} memories from the past",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text("🕰️", fontSize = 48.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (memories.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Outlined.History,
                        "Nothing to look back on… yet",
                        "Keep journaling. Entries from a week, a month or a year ago will resurface here.",
                        Modifier.height(360.dp)
                    )
                }
            }

            memories.forEach { (label, entries) ->
                item(key = label) {
                    Row(Modifier.padding(top = 8.dp)) {
                        SectionHeader(label)
                        Spacer(Modifier.width(0.dp))
                        Text(
                            entries.first().createdAt.toLocalDate().formatDay(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(entries, key = { it.id }) { entry ->
                    EntryCard(
                        entry,
                        onClick = { onOpenEntry(entry.id) },
                        showDate = false,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
