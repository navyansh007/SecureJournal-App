package com.navyanshkesarwani.securejournal.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.navyanshkesarwani.securejournal.data.JournalEntry
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.Mood
import com.navyanshkesarwani.securejournal.ui.components.MoodSelector
import com.navyanshkesarwani.securejournal.ui.components.PastOrTodayDates
import com.navyanshkesarwani.securejournal.ui.components.TimePickerDialog
import com.navyanshkesarwani.securejournal.ui.formatDay
import com.navyanshkesarwani.securejournal.ui.formatTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

private enum class SaveState { IDLE, SAVING, SAVED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(entryId: Long?, onClose: () -> Unit) {
    val original = remember(entryId) { entryId?.let { JournalRepository.get(it) } }
    var id by remember { mutableLongStateOf(original?.id ?: 0L) }
    var title by remember { mutableStateOf(original?.title.orEmpty()) }
    var content by remember { mutableStateOf(original?.content.orEmpty()) }
    var mood by remember { mutableStateOf(original?.mood ?: Mood.NEUTRAL) }
    var createdAt by remember { mutableStateOf(original?.createdAt ?: LocalDateTime.now().withSecond(0).withNano(0)) }
    var saveState by remember { mutableStateOf(SaveState.IDLE) }
    var lastSaved by remember { mutableStateOf<LocalDateTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isEmpty = title.isBlank() && content.isBlank()
    var hasChanges by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Autosave and the Done button can both save; the lock makes sure a new entry is only inserted once.
    val saveLock = remember { Mutex() }

    suspend fun save() = saveLock.withLock {
        if (isEmpty || !hasChanges) return@withLock
        hasChanges = false
        val stored = JournalRepository.save(JournalEntry(id, title.trim(), content.trim(), mood, createdAt))
        id = stored.id
        lastSaved = stored.updatedAt
        saveState = SaveState.SAVED
    }

    // Autosave: write to the database 0.8 s after the user stops typing.
    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(title, content, mood, createdAt) {
        if (firstRun) { firstRun = false; return@LaunchedEffect }
        hasChanges = true
        if (isEmpty) return@LaunchedEffect
        saveState = SaveState.SAVING
        delay(800)
        save()
    }

    var closing by remember { mutableStateOf(false) }
    fun close() {
        if (closing) return // ignore a double tap on Back/Done
        closing = true
        scope.launch {
            save()
            onClose()
        }
    }
    BackHandler { close() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = ::close) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                title = {
                    Column {
                        Text(if (original == null) "New entry" else "Edit entry", style = MaterialTheme.typography.titleMedium)
                        SaveIndicator(saveState, lastSaved)
                    }
                },
                actions = {
                    IconButton(onClick = ::close, enabled = !isEmpty) { Icon(Icons.Filled.Check, "Done") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(createdAt.toLocalDate().formatDay()) },
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text(createdAt.formatTime()) },
                    leadingIcon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)) }
                )
            }
            Text(
                "How are you feeling?",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )
            MoodSelector(selected = mood, onSelect = { mood = it })

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                val fieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title", style = MaterialTheme.typography.headlineSmall) },
                    textStyle = MaterialTheme.typography.headlineSmall,
                    colors = fieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Dear diary…") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Lock, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Saved privately on this device",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                val words = content.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
                Text(
                    "$words word${if (words == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = createdAt.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = PastOrTodayDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        createdAt = date.atTime(createdAt.toLocalTime())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state) }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(createdAt.hour, createdAt.minute)
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                createdAt = createdAt.toLocalDate().atTime(LocalTime.of(state.hour, state.minute))
                showTimePicker = false
            }
        ) { TimePicker(state) }
    }
}

@Composable
private fun SaveIndicator(state: SaveState, lastSaved: LocalDateTime?) {
    val text = when (state) {
        SaveState.IDLE -> "Autosave on"
        SaveState.SAVING -> "Saving…"
        SaveState.SAVED -> "Saved · ${lastSaved?.formatTime().orEmpty()}"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (state == SaveState.SAVED) {
            Icon(Icons.Outlined.CheckCircle, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal
        )
    }
}
