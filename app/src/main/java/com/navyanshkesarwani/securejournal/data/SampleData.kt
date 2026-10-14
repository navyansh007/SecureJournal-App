package com.navyanshkesarwani.securejournal.data

import java.time.LocalDate
import java.time.LocalTime

/**
 * Example entries the user can add on purpose (Settings → "Load sample entries", or the empty Journal screen).
 * Dates are relative to today so the streak and "On this day" screens have something to show.
 * Once added they are normal entries in the database and can be edited or deleted.
 */
object SampleData {

    fun entries(today: LocalDate = LocalDate.now()): List<JournalEntry> {
        fun entry(date: LocalDate, time: String, mood: Mood, title: String, content: String) =
            JournalEntry(title = title, content = content, mood = mood, createdAt = date.atTime(LocalTime.parse(time)))

        return listOf(
            entry(
                today, "08:15", Mood.CALM, "Slow morning",
                "Woke up before the alarm for once. Made filter coffee and sat by the window watching the street wake up. " +
                    "I want more mornings like this — no phone for the first hour."
            ),
            entry(
                today.minusDays(1), "22:40", Mood.GRATEFUL, "Dinner with Riya",
                "Riya cooked her mother's dal recipe and we talked for hours about the trip we keep postponing. " +
                    "Grateful for friends who make time even when life is busy."
            ),
            entry(
                today.minusDays(3), "19:05", Mood.TIRED, "Long day at work",
                "Back-to-back meetings and the release slipped again. Need to block focus time on my calendar " +
                    "and actually protect it."
            ),
            entry(
                today.minusWeeks(1), "07:30", Mood.EXCITED, "First 5K run!",
                "Finished my first 5K without stopping. 31 minutes. Legs are jelly but I feel unstoppable."
            ),
            entry(
                today.minusDays(10), "21:00", Mood.ANXIOUS, "Interview tomorrow",
                "Can't stop rehearsing answers in my head. Reminder to self: I've prepared well. Sleep matters more than one more run-through."
            ),
            entry(
                today.minusWeeks(2), "18:20", Mood.HAPPY, "Got the offer",
                "They called this afternoon — I got the job! Called Mom first. She cried, I cried. What a day."
            ),
            entry(
                today.minusMonths(1), "13:45", Mood.NEUTRAL, "Sunday chores",
                "Laundry, groceries, cleaned the bookshelf. Found an old postcard from Shimla tucked inside a novel."
            ),
            entry(
                today.minusMonths(3), "23:10", Mood.SAD, "Missing home",
                "Rainy evening and the house feels too quiet. Video-called the family; Dadi asked when I'm visiting next."
            ),
            entry(
                today.minusYears(1), "17:00", Mood.HAPPY, "Beach day",
                "Spent the whole afternoon at the beach. Built a terrible sandcastle and ate too much bhel puri."
            ),
            entry(
                today.minusYears(2), "09:00", Mood.GRATEFUL, "Graduation",
                "Walked across the stage today. Four years went by so fast. Thank you to everyone who got me here."
            ),
            entry(
                today.minusMonths(5), "20:30", Mood.ANGRY, "Missed the train",
                "Missed the last train by thirty seconds. Took an expensive cab home and sulked the whole ride."
            ),
        )
    }
}
