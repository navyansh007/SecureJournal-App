package com.navyanshkesarwani.securejournal.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * In-memory stand-in for the encrypted Room database.
 * Backed by snapshot state so the UI recomposes on every change.
 */
object JournalRepository {

    private val _entries = mutableStateListOf<JournalEntry>().apply { addAll(SampleData.entries()) }
    private var nextId = (_entries.maxOfOrNull { it.id } ?: 0L) + 1

    val entries: List<JournalEntry>
        get() = _entries.sortedByDescending { it.createdAt }

    fun get(id: Long): JournalEntry? = _entries.firstOrNull { it.id == id }

    /** Inserts when [entry] has id 0, otherwise updates. Returns the stored entry. */
    fun save(entry: JournalEntry): JournalEntry {
        val now = LocalDateTime.now()
        return if (entry.id == 0L) {
            entry.copy(id = nextId++, updatedAt = now).also { _entries.add(it) }
        } else {
            val updated = entry.copy(updatedAt = now)
            val index = _entries.indexOfFirst { it.id == entry.id }
            if (index >= 0) _entries[index] = updated else _entries.add(updated)
            updated
        }
    }

    fun delete(id: Long) {
        _entries.removeAll { it.id == id }
    }

    fun deleteAll() = _entries.clear()

    fun search(
        query: String,
        moods: Set<Mood> = emptySet(),
        from: LocalDate? = null,
        to: LocalDate? = null,
    ): List<JournalEntry> {
        val q = query.trim()
        return entries.filter { entry ->
            val date = entry.createdAt.toLocalDate()
            (q.isEmpty() || entry.title.contains(q, true) || entry.content.contains(q, true)) &&
                (moods.isEmpty() || entry.mood in moods) &&
                (from == null || !date.isBefore(from)) &&
                (to == null || !date.isAfter(to))
        }
    }

    /** Entries written on this date a few weeks, months or years ago, grouped by label. */
    fun onThisDay(today: LocalDate = LocalDate.now()): List<Pair<String, List<JournalEntry>>> {
        val groups = linkedMapOf<String, MutableList<JournalEntry>>()
        fun add(label: String, entry: JournalEntry) = groups.getOrPut(label) { mutableListOf() }.add(entry)

        entries.forEach { entry ->
            val date = entry.createdAt.toLocalDate()
            if (!date.isBefore(today)) return@forEach
            val weeks = (1..4).firstOrNull { today.minusWeeks(it.toLong()) == date }
            val months = (1..11).firstOrNull { today.minusMonths(it.toLong()) == date }
            val years = today.year - date.year
            when {
                weeks != null -> add(if (weeks == 1) "1 week ago" else "$weeks weeks ago", entry)
                months != null -> add(if (months == 1) "1 month ago" else "$months months ago", entry)
                date.monthValue == today.monthValue && date.dayOfMonth == today.dayOfMonth ->
                    add(if (years == 1) "1 year ago" else "$years years ago", entry)
            }
        }
        // Most recent memories first
        return groups.entries
            .sortedBy { (_, list) -> -list.first().createdAt.toLocalDate().toEpochDay() }
            .map { it.key to it.value.toList() }
    }

    /** Consecutive days (ending today or yesterday) with at least one entry. */
    fun currentStreak(today: LocalDate = LocalDate.now()): Int {
        val days = _entries.map { it.createdAt.toLocalDate() }.toSet()
        var day = if (today in days) today else today.minusDays(1)
        var streak = 0
        while (day in days) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }
}
