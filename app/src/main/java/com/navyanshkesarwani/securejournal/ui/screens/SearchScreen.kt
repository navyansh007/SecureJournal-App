package com.navyanshkesarwani.securejournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.Mood
import com.navyanshkesarwani.securejournal.ui.components.EmptyState
import com.navyanshkesarwani.securejournal.ui.components.EntryCard
import com.navyanshkesarwani.securejournal.ui.components.PastOrTodayDates
import com.navyanshkesarwani.securejournal.ui.formatShortDay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class DateFilter(val label: String) {
    ANY("Any time"), WEEK("Last 7 days"), MONTH("Last 30 days"), YEAR("This year"), CUSTOM("Custom")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onOpenEntry: (Long) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var moods by remember { mutableStateOf(emptySet<Mood>()) }
    var dateFilter by remember { mutableStateOf(DateFilter.ANY) }
    var customRange by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val today = LocalDate.now()
    val (from, to) = when (dateFilter) {
        DateFilter.ANY -> null to null
        DateFilter.WEEK -> today.minusDays(6) to today
        DateFilter.MONTH -> today.minusDays(29) to today
        DateFilter.YEAR -> today.withDayOfYear(1) to today
        DateFilter.CUSTOM -> customRange?.first to customRange?.second
    }
    val results = JournalRepository.search(query, moods, from, to)
    val hasFilters = query.isNotBlank() || moods.isNotEmpty() || dateFilter != DateFilter.ANY

    Scaffold(topBar = { TopAppBar(title = { Text("Search") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search titles and entries") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Clear") }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DateFilter.entries) { filter ->
                    val label = if (filter == DateFilter.CUSTOM && customRange != null && dateFilter == DateFilter.CUSTOM) {
                        "${customRange!!.first.formatShortDay()} – ${customRange!!.second.formatShortDay()}"
                    } else filter.label
                    FilterChip(
                        selected = dateFilter == filter,
                        onClick = {
                            if (filter == DateFilter.CUSTOM) showRangePicker = true else dateFilter = filter
                        },
                        label = { Text(label) },
                        leadingIcon = if (filter == DateFilter.CUSTOM) {
                            { Icon(Icons.Outlined.DateRange, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            FlowRow(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Mood.entries.forEach { mood ->
                    FilterChip(
                        selected = mood in moods,
                        onClick = { moods = if (mood in moods) moods - mood else moods + mood },
                        label = { Text("${mood.emoji} ${mood.label}") }
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (hasFilters) "${results.size} result${if (results.size == 1) "" else "s"}" else "All entries",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (hasFilters) {
                    TextButton(onClick = {
                        query = ""; moods = emptySet(); dateFilter = DateFilter.ANY; customRange = null
                    }) { Text("Clear filters") }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }

            if (results.isEmpty()) {
                EmptyState(Icons.Outlined.SearchOff, "No matching entries", "Try a different keyword, mood or date range.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results, key = { it.id }) { entry ->
                        EntryCard(entry, onClick = { onOpenEntry(entry.id) })
                    }
                }
            }
        }
    }

    if (showRangePicker) {
        val state = rememberDateRangePickerState(selectableDates = PastOrTodayDates)
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(
                    enabled = state.selectedStartDateMillis != null,
                    onClick = {
                        val start = state.selectedStartDateMillis!!.toLocalDate()
                        val end = state.selectedEndDateMillis?.toLocalDate() ?: start
                        customRange = start to end
                        dateFilter = DateFilter.CUSTOM
                        showRangePicker = false
                    }
                ) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showRangePicker = false }) { Text("Cancel") } }
        ) {
            DateRangePicker(state, modifier = Modifier.weight(1f))
        }
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
