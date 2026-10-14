package com.navyanshkesarwani.securejournal.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Single place the UI talks to for journal entries.
 * Entries live in the Room database; [entries] is a live copy that updates whenever the table changes,
 * so every screen that reads it recomposes automatically.
 */
object JournalRepository {

    private lateinit var dao: JournalDao
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** All entries, newest first. */
    var entries by mutableStateOf<List<JournalEntry>>(emptyList())
        private set

    /** False until the database has been read once after app start. */
    var isLoaded by mutableStateOf(false)
        private set

    /** Called once from [com.navyanshkesarwani.securejournal.SecureJournalApplication]. */
    fun init(context: Context) {
        dao = JournalDatabase.create(context).journalDao()
        scope.launch {
            dao.observeAll().collect {
                entries = it
                isLoaded = true
            }
        }
    }

    fun get(id: Long): JournalEntry? = entries.firstOrNull { it.id == id }

    /** Inserts when [entry] has id 0, otherwise updates. Returns the stored entry. */
    suspend fun save(entry: JournalEntry): JournalEntry {
        val stamped = entry.copy(updatedAt = LocalDateTime.now())
        return if (stamped.id == 0L) {
            stamped.copy(id = dao.insert(stamped))
        } else {
            dao.update(stamped)
            stamped
        }
    }

    fun delete(id: Long) {
        scope.launch { dao.delete(id) }
    }

    fun deleteAll() {
        scope.launch { dao.deleteAll() }
    }

    /** Adds entries that aren't already in the journal (same title and time). Returns how many were added. */
    suspend fun addAll(newEntries: List<JournalEntry>): Int {
        val existing = entries.map { it.title to it.createdAt }.toSet()
        val toAdd = newEntries
            .filter { (it.title to it.createdAt) !in existing }
            .map { it.copy(id = 0) }
        dao.insertAll(toAdd)
        return toAdd.size
    }

    // ---------- Backup file (JSON) ----------

    fun toJson(): String {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("title", entry.title)
                    .put("content", entry.content)
                    .put("mood", entry.mood.name)
                    .put("createdAt", entry.createdAt.toString())
                    .put("updatedAt", entry.updatedAt.toString())
            )
        }
        return JSONObject().put("app", "SecureJournal").put("entries", array).toString(2)
    }

    /** Throws if [json] is not a SecureJournal backup. */
    fun fromJson(json: String): List<JournalEntry> {
        val root = JSONObject(json)
        require(root.optString("app") == "SecureJournal") { "Not a SecureJournal backup" }
        val array = root.getJSONArray("entries")
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            JournalEntry(
                title = item.getString("title"),
                content = item.getString("content"),
                mood = Mood.valueOf(item.getString("mood")),
                createdAt = LocalDateTime.parse(item.getString("createdAt")),
                updatedAt = LocalDateTime.parse(item.getString("updatedAt")),
            )
        }
    }

    // ---------- Queries used by the screens ----------

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
        val days = entries.map { it.createdAt.toLocalDate() }.toSet()
        var day = if (today in days) today else today.minusDays(1)
        var streak = 0
        while (day in days) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }
}
