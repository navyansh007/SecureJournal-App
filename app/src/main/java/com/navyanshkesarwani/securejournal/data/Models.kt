package com.navyanshkesarwani.securejournal.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

enum class Mood(val emoji: String, val label: String, val color: Color) {
    HAPPY("😊", "Happy", Color(0xFFF6C453)),
    EXCITED("🤩", "Excited", Color(0xFFF08A5D)),
    GRATEFUL("🙏", "Grateful", Color(0xFF8BC48A)),
    CALM("😌", "Calm", Color(0xFF7FB7D9)),
    NEUTRAL("😐", "Neutral", Color(0xFFB0AFA8)),
    TIRED("😴", "Tired", Color(0xFF9C8FC4)),
    SAD("😢", "Sad", Color(0xFF6C8EBF)),
    ANXIOUS("😰", "Anxious", Color(0xFFC7A27C)),
    ANGRY("😠", "Angry", Color(0xFFD9645A)),
}

data class JournalEntry(
    val id: Long,
    val title: String,
    val content: String,
    val mood: Mood,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime = createdAt,
)
