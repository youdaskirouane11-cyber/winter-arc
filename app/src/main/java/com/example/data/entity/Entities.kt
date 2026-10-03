package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val id: Long = 1L,
    val name: String = "Young Athlete",
    val email: String = "athlete@discipline.pro",
    @ColumnInfo(name = "is_dark_mode") val isDarkMode: Boolean = true,
    @ColumnInfo(name = "daily_bonus_xp") val dailyBonusXp: Int = 50,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "running_sessions",
    indices = [Index("user_id"), Index("date")]
)
data class RunningSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val duration: Int, // minutes
    val distance: Double, // kilometers
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_sessions",
    indices = [Index("user_id"), Index("date")]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val duration: Int, // minutes
    val subject: String,
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "gym_sessions",
    indices = [Index("user_id"), Index("date")]
)
data class GymSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val duration: Int, // minutes
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "prayer_records",
    indices = [
        Index("user_id"),
        Index("date"),
        Index(value = ["user_id", "date"], unique = true)
    ]
)
data class PrayerRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val completed: Boolean,
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "business_sessions",
    indices = [Index("user_id"), Index("date")]
)
data class BusinessSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val duration: Int, // minutes
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "football_sessions",
    indices = [Index("user_id"), Index("date")]
)
data class FootballSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val duration: Int, // minutes
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "xp_transactions",
    indices = [
        Index("user_id"),
        Index("date"),
        Index(value = ["reference_id"], unique = true)
    ]
)
data class XpTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long = 1L,
    val source: String, // running, study, gym, prayer, business, football, daily_bonus
    val amount: Int,
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "reference_id") val referenceId: String
)
