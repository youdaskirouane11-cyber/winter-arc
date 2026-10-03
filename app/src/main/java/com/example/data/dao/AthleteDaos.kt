package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BusinessSession
import com.example.data.entity.FootballSession
import com.example.data.entity.GymSession
import com.example.data.entity.PrayerRecord
import com.example.data.entity.RunningSession
import com.example.data.entity.StudySession
import com.example.data.entity.UserProfile
import com.example.data.entity.XpTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RunningDao {
    @Query("SELECT * FROM running_sessions WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<RunningSession>>

    @Query("SELECT * FROM running_sessions WHERE user_id = :userId AND date = :date")
    suspend fun getSessionsByDate(userId: Long = 1L, date: String): List<RunningSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RunningSession): Long

    @Query("DELETE FROM running_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM running_sessions")
    suspend fun deleteAll()
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_sessions WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySession): Long

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}

@Dao
interface GymDao {
    @Query("SELECT * FROM gym_sessions WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<GymSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: GymSession): Long

    @Query("DELETE FROM gym_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM gym_sessions")
    suspend fun deleteAll()
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_records WHERE user_id = :userId ORDER BY date DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<PrayerRecord>>

    @Query("SELECT * FROM prayer_records WHERE user_id = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: Long = 1L, date: String): PrayerRecord?

    @Query("SELECT * FROM prayer_records WHERE user_id = :userId AND date = :date LIMIT 1")
    fun getByDateFlow(userId: Long = 1L, date: String): Flow<PrayerRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: PrayerRecord): Long

    @Query("DELETE FROM prayer_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM prayer_records")
    suspend fun deleteAll()
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM business_sessions WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<BusinessSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: BusinessSession): Long

    @Query("DELETE FROM business_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM business_sessions")
    suspend fun deleteAll()
}

@Dao
interface FootballDao {
    @Query("SELECT * FROM football_sessions WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<FootballSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FootballSession): Long

    @Query("DELETE FROM football_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM football_sessions")
    suspend fun deleteAll()
}

@Dao
interface XpDao {
    @Query("SELECT * FROM xp_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllFlow(userId: Long = 1L): Flow<List<XpTransaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions WHERE user_id = :userId")
    fun getTotalXpFlow(userId: Long = 1L): Flow<Int>

    @Query("SELECT COUNT(*) > 0 FROM xp_transactions WHERE reference_id = :referenceId")
    suspend fun hasTransactionWithRef(referenceId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: XpTransaction): Long

    @Query("DELETE FROM xp_transactions")
    suspend fun deleteAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserFlow(id: Long = 1L): Flow<UserProfile?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUser(id: Long = 1L): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserProfile)
}
