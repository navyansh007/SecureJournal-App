package com.navyanshkesarwani.securejournal.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/** SQL queries for the entries table. Room generates the code behind these functions. */
@Dao
interface JournalDao {
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<JournalEntry>>

    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Insert
    suspend fun insertAll(entries: List<JournalEntry>)

    @Update
    suspend fun update(entry: JournalEntry)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM entries")
    suspend fun deleteAll()
}

/** Room can't store LocalDateTime directly, so it is saved as text such as "2026-10-28T21:30". */
class Converters {
    @TypeConverter
    fun fromDateTime(value: LocalDateTime): String = value.toString()

    @TypeConverter
    fun toDateTime(value: String): LocalDateTime = LocalDateTime.parse(value)
}

@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        fun create(context: Context): JournalDatabase =
            Room.databaseBuilder(context, JournalDatabase::class.java, "journal.db").build()
    }
}
