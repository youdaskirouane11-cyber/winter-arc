package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BusinessDao
import com.example.data.dao.FootballDao
import com.example.data.dao.GymDao
import com.example.data.dao.PrayerDao
import com.example.data.dao.RunningDao
import com.example.data.dao.StudyDao
import com.example.data.dao.UserDao
import com.example.data.dao.XpDao
import com.example.data.entity.BusinessSession
import com.example.data.entity.FootballSession
import com.example.data.entity.GymSession
import com.example.data.entity.PrayerRecord
import com.example.data.entity.RunningSession
import com.example.data.entity.StudySession
import com.example.data.entity.UserProfile
import com.example.data.entity.XpTransaction

@Database(
    entities = [
        UserProfile::class,
        RunningSession::class,
        StudySession::class,
        GymSession::class,
        PrayerRecord::class,
        BusinessSession::class,
        FootballSession::class,
        XpTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runningDao(): RunningDao
    abstract fun studyDao(): StudyDao
    abstract fun gymDao(): GymDao
    abstract fun prayerDao(): PrayerDao
    abstract fun businessDao(): BusinessDao
    abstract fun footballDao(): FootballDao
    abstract fun xpDao(): XpDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "athlete_engine.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
