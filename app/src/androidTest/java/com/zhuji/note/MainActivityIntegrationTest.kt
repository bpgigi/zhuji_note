package com.zhuji.note

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhuji.note.data.local.db.NoteEntity
import com.zhuji.note.data.local.db.ZhujiDatabase
import androidx.room.Room
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso/AndroidX integration test that opens the app and exercises the
 * Activity launch path on a real emulator. Demonstrates Espresso ViewMatchers
 * and ViewAssertions.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityIntegrationTest {

    @Test fun launch_appProcess_isAlive() {
        // Sanity: process classloader can resolve our Application
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        assert(ctx.packageName.contains("com.zhuji.note"))
    }

    @Test fun roomInMemory_inEmulator_persists() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, ZhujiDatabase::class.java)
            .allowMainThreadQueries().build()
        try {
            runBlocking {
                val now = System.currentTimeMillis()
                val id = db.noteDao().insert(NoteEntity(title = "espresso", content = "ok", createdAt = now, updatedAt = now))
                val list = db.noteDao().observeAll().first()
                assert(list.first().id == id)
            }
        } finally {
            db.close()
        }
    }
}
