package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.AthleteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Athlete Engine", appName)
    }

    @Test
    fun `level calculations work accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val repository = AthleteRepository(db)

        val beginner = repository.calculateLevel(50)
        assertEquals(1, beginner.level)
        assertEquals("Beginner", beginner.title)

        val dedicated = repository.calculateLevel(750)
        assertEquals(6, dedicated.level)
        assertEquals("Dedicated", dedicated.title)

        val disciplined = repository.calculateLevel(1800)
        assertEquals(13, disciplined.level)
        assertEquals("Disciplined", disciplined.title)

        val elite = repository.calculateLevel(3200)
        assertTrue(elite.level >= 20)
        assertEquals("Elite Athlete", elite.title)
    }
}
