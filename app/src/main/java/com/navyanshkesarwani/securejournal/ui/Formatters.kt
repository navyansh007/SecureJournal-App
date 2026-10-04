package com.navyanshkesarwani.securejournal.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val dayFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
private val longDayFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
private val timeFormat = DateTimeFormatter.ofPattern("h:mm a")
private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
private val shortDayFormat = DateTimeFormatter.ofPattern("d MMM")

fun LocalDate.formatDay(): String = format(dayFormat)
fun LocalDate.formatLongDay(): String = format(longDayFormat)
fun LocalDate.formatShortDay(): String = format(shortDayFormat)
fun LocalDate.formatMonth(): String = format(monthFormat)
fun LocalTime.formatTime(): String = format(timeFormat)
fun LocalDateTime.formatTime(): String = format(timeFormat)
fun LocalDateTime.formatDateTime(): String = "${toLocalDate().formatDay()} · ${format(timeFormat)}"

fun LocalDate.relativeLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> formatDay()
}
