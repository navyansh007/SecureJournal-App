# SecureJournal

A private diary for Android, locked with a PIN or fingerprint. Built with Kotlin and Jetpack Compose.

Everything the app shows comes from the user or from the device. There is no hard-coded journal data:
entries are saved in a Room (SQLite) database, settings in SharedPreferences, and nothing leaves the phone
unless the user makes a backup file.

## Demo walkthrough

1. **Onboarding**: three intro pages (or tap *Skip*).
2. **Create PIN**: enter 4 digits, then confirm them.
3. **Biometrics**: *Enable biometrics* opens the real Android fingerprint dialog. If the phone has no
   fingerprint set up, the button is disabled and the screen explains why. Tap *Not now* to skip.
4. **Journal (Home)**: empty on first run. Tap **Load sample entries** to fill it with 11 example entries
   (saved to the database like any other entry), or tap **Write** to add your own.
5. **Editor**: date/time pickers, nine moods, word count. It autosaves 0.8 s after you stop typing.
6. **Entry detail**: share (Android share sheet), edit, delete with confirmation.
7. **Search**: keyword, quick date ranges or a custom range, and multi-select mood filters.
8. **On this day**: entries from 1–4 weeks, 1–11 months, or N years ago today.
9. **Settings**: change PIN, biometric unlock, auto-lock, lock now, hide in recent apps, daily reminder
   and time, test reminder, theme, backup & restore, export, load sample entries, delete all.
10. **Backup & restore**: *Back up now* opens the system "Save" screen (choose Downloads or Google Drive).
    *Restore from backup* opens the system file picker and adds the entries back, skipping duplicates.

Close the app (Home button) and reopen it to see the lock screen. Force-stop it and reopen: your entries
and settings are still there.

**Wrong PIN:** after 5 wrong attempts the PIN pad is blocked for 30 seconds (this survives an app restart).

## How each feature works (Android components)

| Feature | Android component | File |
|---|---|---|
| Entries stored on the phone | **Room** database: `@Entity`, `@Dao`, `@Database`, `Flow` | `data/JournalDatabase.kt`, `data/Models.kt` |
| Screens update when data changes | Room `Flow` collected into Compose state | `data/JournalRepository.kt` |
| PIN, theme, reminder, auto-lock saved | **SharedPreferences** | `data/SettingsStore.kt` |
| App-wide setup at launch | **Application** class | `SecureJournalApplication.kt` |
| Lock when the app goes to background | **Activity lifecycle** `onStop` / `onStart` | `MainActivity.kt` |
| Fingerprint / face unlock | **BiometricPrompt** (`androidx.biometric`) | `ui/components/Biometric.kt` |
| Hide from recent apps, block screenshots | `WindowManager.LayoutParams.FLAG_SECURE` | `MainActivity.kt` |
| Daily reminder | **WorkManager** periodic work + **NotificationManager** + notification channel | `reminder/Reminder.kt` |
| Notification permission (Android 13+) | Runtime permission `POST_NOTIFICATIONS` | `ui/screens/SettingsScreen.kt` |
| Backup file to Downloads / Google Drive | **Storage Access Framework** (`CreateDocument`, `OpenDocument`) + `ContentResolver` | `ui/screens/BackupScreen.kt` |
| Backup file format | JSON with Android's built-in `org.json` | `data/JournalRepository.kt` |
| Share an entry / export all | `Intent.ACTION_SEND` | `EntryDetailScreen.kt`, `SettingsScreen.kt` |
| Journal never copied by Android's auto-backup | `allowBackup="false"` + backup rules | `AndroidManifest.xml`, `res/xml/` |
| Screens and navigation | Jetpack Compose, Material 3, Navigation Compose | `ui/` |

## Structure

```
app/src/main/java/com/navyanshkesarwani/securejournal/
├── SecureJournalApplication.kt  # loads settings and opens the database at app start
├── MainActivity.kt              # theme, system bars, FLAG_SECURE, auto-lock on return
├── data/
│   ├── Models.kt                # JournalEntry (Room entity) and Mood
│   ├── JournalDatabase.kt       # Room DAO + database
│   ├── JournalRepository.kt     # what screens call: save, delete, search, streak, backup JSON
│   ├── SettingsStore.kt         # SharedPreferences settings + Session (lock state, PIN check)
│   └── SampleData.kt            # optional example entries the user can load
├── reminder/Reminder.kt         # WorkManager worker + notification
└── ui/
    ├── SecureJournalApp.kt      # setup flow → lock screen → app
    ├── navigation/              # routes, bottom nav, NavHost
    ├── components/              # entry card, mood picker, PIN pad, BiometricPrompt helper
    ├── screens/                 # home, editor, detail, search, on this day, settings, backup
    │   └── auth/                # onboarding, PIN setup, biometric setup, lock
    └── theme/
```

## Tips for the demo

- **Screenshots / screen recording show a black screen?** That is `FLAG_SECURE` doing its job.
  Turn off *Settings → Hide in recent apps* while recording.
- **Fingerprint on the emulator:** enroll a fingerprint in the emulator's *Settings → Security*, then in
  SecureJournal's lock screen use the emulator's *Extended controls → Fingerprint → Touch sensor*
  (or run `adb -e emu finger touch 1`).
- **Reminder:** turn on *Daily reminder*, allow notifications, then tap *Send test reminder* to see it now.

## Build

```
./gradlew assembleDebug
```
