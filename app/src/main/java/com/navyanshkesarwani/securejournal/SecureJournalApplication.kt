package com.navyanshkesarwani.securejournal

import android.app.Application
import com.navyanshkesarwani.securejournal.data.JournalRepository
import com.navyanshkesarwani.securejournal.data.SettingsStore

/** Runs before any screen: loads saved settings and opens the database. */
class SecureJournalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsStore.init(this)
        JournalRepository.init(this)
    }
}
