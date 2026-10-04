# SecureJournal

A private, PIN/biometric-locked diary for Android, built with Jetpack Compose.

> **Status: UI prototype.** Every screen and flow is clickable, but data is held in memory
> and the security/cloud features are simulated. Nothing is persisted or sent anywhere yet.

## Demo walkthrough

1. **Onboarding** – three intro pages (or tap *Skip*).
2. **Create PIN** – enter any 4 digits, then confirm.
3. **Biometrics** – *Enable biometrics* opens a mock prompt; tap the fingerprint icon to "scan".
4. **Journal (Home)** – streak/stats, an *On this day* teaser and entries grouped by month. Tap **Write** to add one.
5. **Editor** – date/time pickers, mood selector, autosave indicator, word count.
6. **Entry detail** – share (real Android share sheet), edit, delete with confirmation.
7. **Search** – keyword, quick date ranges or a custom range, and multi-select mood filters.
8. **On this day** – entries from 1–4 weeks, 1–11 months, or N years ago today.
9. **Settings** – change PIN, biometric toggle, auto-lock, lock now, daily reminder + time,
   theme (System/Light/Dark), cloud backup, export, delete all.
10. **Cloud backup & sync** – mock sign-in (any valid-looking email + 6-char password), back up, restore, sign out.

Press Home and reopen the app to see the lock screen (auto-lock defaults to *Immediately*).
Sample entries are generated relative to today so every screen has content.

## What is simulated (to be replaced in production)

| Prototype | Planned implementation |
|---|---|
| `JournalRepository` (in-memory list) | Room database, encrypted at rest |
| `SettingsStore` (in-memory state) | SharedPreferences / EncryptedSharedPreferences |
| `SimulatedBiometricDialog` | `BiometricPrompt` API |
| Reminder preview snackbar | WorkManager + NotificationManager |
| Cloud sign-in / backup progress | Firebase Auth + Firestore (client-side encrypted) |

## Structure

```
app/src/main/java/com/navyanshkesarwani/securejournal/
├── MainActivity.kt          # theme, system bars, auto-lock on resume
├── data/                    # models, sample data, in-memory repository & settings
└── ui/
    ├── SecureJournalApp.kt  # setup flow → lock gate → app
    ├── navigation/          # routes, bottom nav, NavHost
    ├── components/          # entry card, mood picker, PIN pad, dialogs
    ├── screens/             # home, editor, detail, search, on this day, settings, cloud
    │   └── auth/            # onboarding, PIN setup, biometric setup, lock
    └── theme/
```

## Build

```
./gradlew assembleDebug
```
