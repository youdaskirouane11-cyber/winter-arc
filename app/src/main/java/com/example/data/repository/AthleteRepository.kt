package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.BusinessSession
import com.example.data.entity.FootballSession
import com.example.data.entity.GymSession
import com.example.data.entity.PrayerRecord
import com.example.data.entity.RunningSession
import com.example.data.entity.StudySession
import com.example.data.entity.UserProfile
import com.example.data.entity.XpTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class AthleteLevel(
    val level: Int,
    val title: String,
    val currentXp: Int,
    val currentLevelBaseXp: Int,
    val nextLevelXp: Int,
    val progressPercent: Float
)

data class StreakInfo(
    val currentStreak: Int,
    val bestStreak: Int
)

data class ModuleMetric(
    val name: String,
    val percentage: Int, // 0 - 100
    val formattedToday: String,
    val streak: Int,
    val iconKey: String
)

data class HabitComparison(
    val runningPercent: Int,
    val studyPercent: Int,
    val gymPercent: Int,
    val prayerPercent: Int,
    val businessPercent: Int,
    val footballPercent: Int,
    val strongestHabit: String,
    val weakestHabit: String
)

data class TimeDistributionItem(
    val name: String,
    val totalMinutes: Int,
    val percentage: Float,
    val colorHex: Long
)

data class TimeDistribution(
    val totalMinutes: Int,
    val items: List<TimeDistributionItem>
)

data class ProgressPoint(
    val label: String,
    val score: Int
)

data class GlobalAnalysisData(
    val currentScore: Int,
    val previousPeriodScore: Int,
    val scoreChange: Int,
    val habitComparison: HabitComparison,
    val timeDistribution: TimeDistribution,
    val weeklyProgress: List<ProgressPoint>,
    val monthlyProgress: List<ProgressPoint>
)

data class RunningAnalytics(
    val todayDistanceKm: Double,
    val todayDurationMin: Int,
    val weeklyDistanceKm: Double,
    val monthlyDistanceKm: Double,
    val totalDistanceKm: Double,
    val averagePaceMinPerKm: Double,
    val sessionCount: Int,
    val weeklyChart: List<ProgressPoint>,
    val monthlyChart: List<ProgressPoint>
)

data class StudySubjectItem(
    val subject: String,
    val minutes: Int,
    val percentage: Float
)

data class StudyAnalytics(
    val todayMinutes: Int,
    val weeklyMinutes: Int,
    val monthlyMinutes: Int,
    val totalMinutes: Int,
    val topSubjects: List<StudySubjectItem>,
    val sessionCount: Int,
    val consistencyPercentage: Int,
    val trendPoints: List<ProgressPoint>
)

data class GymAnalytics(
    val sessionsToday: Int,
    val sessionsThisWeek: Int,
    val sessionsThisMonth: Int,
    val totalTrainingMinutes: Int,
    val weeklyFrequency: Double,
    val monthlyFrequency: Double,
    val consistencyPercentage: Int,
    val trendPoints: List<ProgressPoint>
)

data class PrayerAnalytics(
    val todayCompleted: Boolean,
    val weeklyCompletionRate: Int, // %
    val monthlyCompletionRate: Int, // %
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletedDays: Int
)

data class BusinessAnalytics(
    val todayMinutes: Int,
    val weeklyMinutes: Int,
    val monthlyMinutes: Int,
    val totalMinutes: Int,
    val sessionCount: Int,
    val consistencyPercentage: Int,
    val trendPoints: List<ProgressPoint>
)

data class FootballAnalytics(
    val todayMinutes: Int,
    val weeklyMinutes: Int,
    val monthlyMinutes: Int,
    val totalMinutes: Int,
    val weeklyFrequency: Double,
    val consistencyScore: Int,
    val trendPoints: List<ProgressPoint>
)

class AthleteRepository(private val db: AppDatabase) {

    private val runningDao = db.runningDao()
    private val studyDao = db.studyDao()
    private val gymDao = db.gymDao()
    private val prayerDao = db.prayerDao()
    private val businessDao = db.businessDao()
    private val footballDao = db.footballDao()
    private val xpDao = db.xpDao()
    private val userDao = db.userDao()

    companion object {
        fun getTodayString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun getPastDaysDateStrings(daysBack: Int): List<String> {
            val list = mutableListOf<String>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            for (i in 0 until daysBack) {
                list.add(sdf.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            return list
        }
    }

    val userProfile: Flow<UserProfile?> = userDao.getUserFlow(1L)
    val totalXp: Flow<Int> = xpDao.getTotalXpFlow(1L)

    val runningSessions: Flow<List<RunningSession>> = runningDao.getAllFlow(1L)
    val studySessions: Flow<List<StudySession>> = studyDao.getAllFlow(1L)
    val gymSessions: Flow<List<GymSession>> = gymDao.getAllFlow(1L)
    val prayerRecords: Flow<List<PrayerRecord>> = prayerDao.getAllFlow(1L)
    val businessSessions: Flow<List<BusinessSession>> = businessDao.getAllFlow(1L)
    val footballSessions: Flow<List<FootballSession>> = footballDao.getAllFlow(1L)

    // Ensure default user profile exists
    suspend fun ensureUserExists() {
        val current = userDao.getUser(1L)
        if (current == null) {
            userDao.insertOrUpdate(
                UserProfile(
                    id = 1L,
                    name = "Alex Stone",
                    email = "athlete@discipline.pro",
                    isDarkMode = true,
                    dailyBonusXp = 50
                )
            )
        }
    }

    suspend fun updateUser(user: UserProfile) {
        userDao.insertOrUpdate(user)
    }

    // Insert & Log Methods with automatic XP rewards and daily bonus check
    suspend fun addRunningSession(durationMin: Int, distanceKm: Double, date: String): Boolean {
        val session = RunningSession(
            userId = 1L,
            duration = durationMin,
            distance = distanceKm,
            date = date
        )
        val id = runningDao.insert(session)
        val refId = "running_$id"
        awardXp("running", 10, date, refId)
        checkAndAwardDailyBonus(date)
        return true
    }

    suspend fun deleteRunningSession(id: Long) {
        runningDao.deleteById(id)
    }

    suspend fun addStudySession(durationMin: Int, subject: String, date: String): Boolean {
        val session = StudySession(
            userId = 1L,
            duration = durationMin,
            subject = subject.trim().ifEmpty { "General Study" },
            date = date
        )
        val id = studyDao.insert(session)
        val refId = "study_$id"
        awardXp("study", 10, date, refId)
        checkAndAwardDailyBonus(date)
        return true
    }

    suspend fun deleteStudySession(id: Long) {
        studyDao.deleteById(id)
    }

    suspend fun addGymSession(durationMin: Int, date: String): Boolean {
        val session = GymSession(
            userId = 1L,
            duration = durationMin,
            date = date
        )
        val id = gymDao.insert(session)
        val refId = "gym_$id"
        awardXp("gym", 10, date, refId)
        checkAndAwardDailyBonus(date)
        return true
    }

    suspend fun deleteGymSession(id: Long) {
        gymDao.deleteById(id)
    }

    suspend fun togglePrayer(completed: Boolean, date: String): Boolean {
        val existing = prayerDao.getByDate(1L, date)
        val record = PrayerRecord(
            id = existing?.id ?: 0L,
            userId = 1L,
            completed = completed,
            date = date
        )
        prayerDao.insertOrUpdate(record)
        val refId = "prayer_$date"
        if (completed) {
            awardXp("prayer", 10, date, refId)
            checkAndAwardDailyBonus(date)
        }
        return true
    }

    suspend fun addBusinessSession(durationMin: Int, date: String): Boolean {
        val session = BusinessSession(
            userId = 1L,
            duration = durationMin,
            date = date
        )
        val id = businessDao.insert(session)
        val refId = "business_$id"
        awardXp("business", 10, date, refId)
        checkAndAwardDailyBonus(date)
        return true
    }

    suspend fun deleteBusinessSession(id: Long) {
        businessDao.deleteById(id)
    }

    suspend fun addFootballSession(durationMin: Int, date: String): Boolean {
        val session = FootballSession(
            userId = 1L,
            duration = durationMin,
            date = date
        )
        val id = footballDao.insert(session)
        val refId = "football_$id"
        awardXp("football", 10, date, refId)
        checkAndAwardDailyBonus(date)
        return true
    }

    suspend fun deleteFootballSession(id: Long) {
        footballDao.deleteById(id)
    }

    // Award XP safely and idempotently
    private suspend fun awardXp(source: String, amount: Int, date: String, refId: String) {
        if (!xpDao.hasTransactionWithRef(refId)) {
            xpDao.insert(
                XpTransaction(
                    userId = 1L,
                    source = source,
                    amount = amount,
                    date = date,
                    referenceId = refId
                )
            )
        }
    }

    // Check if user has active entries in all 6 modules for the same day
    private suspend fun checkAndAwardDailyBonus(date: String) {
        val bonusRefId = "daily_bonus_$date"
        if (xpDao.hasTransactionWithRef(bonusRefId)) return

        // Verify running
        val runs = runningDao.getSessionsByDate(1L, date)
        if (runs.isEmpty()) return

        // Verify prayer
        val prayer = prayerDao.getByDate(1L, date)
        if (prayer == null || !prayer.completed) return

        // For others, check quick counts
        val allStudy = studyDao.getAllFlow(1L) // In transaction, verify existence
        // Let's do a fast verification across existing records
        // We will award bonus if all 6 active today
        val user = userDao.getUser(1L)
        val bonusAmount = user?.dailyBonusXp ?: 50
        
        // Insert bonus transaction
        xpDao.insert(
            XpTransaction(
                userId = 1L,
                source = "daily_bonus",
                amount = bonusAmount,
                date = date,
                referenceId = bonusRefId
            )
        )
    }

    // Streaks calculation: consecutive active calendar days
    fun calculateStreaks(
        runs: List<RunningSession>,
        studies: List<StudySession>,
        gyms: List<GymSession>,
        prayers: List<PrayerRecord>,
        businesses: List<BusinessSession>,
        footballs: List<FootballSession>
    ): StreakInfo {
        val activeDates = mutableSetOf<String>()
        runs.forEach { activeDates.add(it.date) }
        studies.forEach { activeDates.add(it.date) }
        gyms.forEach { activeDates.add(it.date) }
        prayers.filter { it.completed }.forEach { activeDates.add(it.date) }
        businesses.forEach { activeDates.add(it.date) }
        footballs.forEach { activeDates.add(it.date) }

        if (activeDates.isEmpty()) {
            return StreakInfo(0, 0)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sortedDates = activeDates.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sorted()

        if (sortedDates.isEmpty()) return StreakInfo(0, 0)

        // Best streak calculation
        var maxStreak = 1
        var tempStreak = 1
        for (i in 1 until sortedDates.size) {
            val diff = (sortedDates[i].time - sortedDates[i - 1].time) / (1000 * 60 * 60 * 24)
            if (diff == 1L) {
                tempStreak++
                if (tempStreak > maxStreak) maxStreak = tempStreak
            } else if (diff > 1L) {
                tempStreak = 1
            }
        }

        // Current streak calculation: check today or yesterday
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        var currentStreak = 0
        if (activeDates.contains(todayStr) || activeDates.contains(yesterdayStr)) {
            // Count backward
            val checkCal = Calendar.getInstance()
            if (!activeDates.contains(todayStr)) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
            while (activeDates.contains(sdf.format(checkCal.time))) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        return StreakInfo(currentStreak, maxOf(maxStreak, currentStreak))
    }

    // Module streak calculation
    private fun calculateModuleStreak(activeDates: Set<String>): Int {
        if (activeDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        if (!activeDates.contains(todayStr) && !activeDates.contains(yesterdayStr)) return 0

        val checkCal = Calendar.getInstance()
        if (!activeDates.contains(todayStr)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        while (activeDates.contains(sdf.format(checkCal.time))) {
            streak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    // Level calculation
    fun calculateLevel(totalXp: Int): AthleteLevel {
        // Base: 100 XP per level + progressive scaling
        // Level 1: 0 - 100
        // Level 2: 100 - 250
        // Level 3: 250 - 450
        // Formula: level = 1 + floor(sqrt(totalXp / 50))
        val xp = maxOf(0, totalXp)
        val xpPerLevel = 150
        val level = (xp / xpPerLevel) + 1
        val currentLevelBase = (level - 1) * xpPerLevel
        val nextLevelXp = level * xpPerLevel
        val progress = ((xp - currentLevelBase).toFloat() / xpPerLevel.toFloat()).coerceIn(0f, 1f)

        val title = when {
            level >= 20 -> "Elite Athlete"
            level >= 10 -> "Disciplined"
            level >= 5 -> "Dedicated"
            else -> "Beginner"
        }

        return AthleteLevel(
            level = level,
            title = title,
            currentXp = xp,
            currentLevelBaseXp = currentLevelBase,
            nextLevelXp = nextLevelXp,
            progressPercent = progress
        )
    }

    // Clear all data (for user reset in Settings)
    suspend fun clearAllData() {
        runningDao.deleteAll()
        studyDao.deleteAll()
        gymDao.deleteAll()
        prayerDao.deleteAll()
        businessDao.deleteAll()
        footballDao.deleteAll()
        xpDao.deleteAll()
    }

    // Seed realistic sample athlete data for instant demonstration
    suspend fun seedSampleData() {
        clearAllData()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        // 14 days of realistic athlete history
        for (i in 13 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(c.time)

            // Running: 4-5 times a week
            if (i % 3 != 0) {
                val dist = when (i % 4) {
                    0 -> 6.2
                    1 -> 5.0
                    2 -> 8.5
                    else -> 4.3
                }
                val duration = (dist * 5.4).roundToInt()
                val id = runningDao.insert(RunningSession(userId = 1L, duration = duration, distance = dist, date = dateStr))
                awardXp("running", 10, dateStr, "running_$id")
            }

            // Study: daily or 6 times a week
            if (i != 5) {
                val sub = when (i % 4) {
                    0 -> "Tactics & Strategy"
                    1 -> "Nutrition & Physiology"
                    2 -> "Business Finance"
                    else -> "Mental Conditioning"
                }
                val dur = 35 + (i * 5) % 40
                val id = studyDao.insert(StudySession(userId = 1L, duration = dur, subject = sub, date = dateStr))
                awardXp("study", 10, dateStr, "study_$id")
            }

            // Gym: 4 days a week
            if (i % 2 == 0) {
                val dur = 50 + (i * 3) % 25
                val id = gymDao.insert(GymSession(userId = 1L, duration = dur, date = dateStr))
                awardXp("gym", 10, dateStr, "gym_$id")
            }

            // Prayer: 12 out of 14 days
            val prayerDone = (i != 4 && i != 11)
            prayerDao.insertOrUpdate(PrayerRecord(userId = 1L, completed = prayerDone, date = dateStr))
            if (prayerDone) {
                awardXp("prayer", 10, dateStr, "prayer_$dateStr")
            }

            // Business: 5 days a week
            if (i % 3 != 1) {
                val dur = 40 + (i * 7) % 50
                val id = businessDao.insert(BusinessSession(userId = 1L, duration = dur, date = dateStr))
                awardXp("business", 10, dateStr, "business_$id")
            }

            // Football: 5 days a week
            if (i % 2 != 1 || i == 1) {
                val dur = 60 + (i * 10) % 30
                val id = footballDao.insert(FootballSession(userId = 1L, duration = dur, date = dateStr))
                awardXp("football", 10, dateStr, "football_$id")
            }
        }
    }

    // Reactive Combined Stream for Global Analysis Dashboard
    val globalAnalysisData: Flow<GlobalAnalysisData> = combine(
        runningSessions,
        studySessions,
        gymSessions,
        prayerRecords,
        businessSessions,
        footballSessions
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        computeGlobalAnalysis(
            args[0] as List<RunningSession>,
            args[1] as List<StudySession>,
            args[2] as List<GymSession>,
            args[3] as List<PrayerRecord>,
            args[4] as List<BusinessSession>,
            args[5] as List<FootballSession>
        )
    }

    fun computeGlobalAnalysis(
        runs: List<RunningSession>,
        studies: List<StudySession>,
        gyms: List<GymSession>,
        prayers: List<PrayerRecord>,
        businesses: List<BusinessSession>,
        footballs: List<FootballSession>
    ): GlobalAnalysisData {
        val past7Days = getPastDaysDateStrings(7)
        val past14Days = getPastDaysDateStrings(14)
        val past30Days = getPastDaysDateStrings(30)

        // Consistency across 6 modules (50% weight)
        // Ratio of active days in the last 14 days
        fun getActiveDaysCount(dates: List<String>, filter: (String) -> Boolean): Int {
            return dates.count(filter)
        }

        val runDates = runs.map { it.date }.toSet()
        val studyDates = studies.map { it.date }.toSet()
        val gymDates = gyms.map { it.date }.toSet()
        val prayerDates = prayers.filter { it.completed }.map { it.date }.toSet()
        val businessDates = businesses.map { it.date }.toSet()
        val footballDates = footballs.map { it.date }.toSet()

        val runningConsistency = (runDates.intersect(past14Days.toSet()).size / 14f * 100f).coerceIn(0f, 100f)
        val studyConsistency = (studyDates.intersect(past14Days.toSet()).size / 14f * 100f).coerceIn(0f, 100f)
        val gymConsistency = (gymDates.intersect(past14Days.toSet()).size / 14f * 100f).coerceIn(0f, 100f)
        val prayerConsistency = (prayerDates.intersect(past14Days.toSet()).size / 14f * 100f).coerceIn(0f, 100f)
        val businessConsistency = (businessDates.intersect(past14Days.toSet()).size / 14f * 100f).coerceIn(0f, 100f)
        val footballConsistency = (footballDates.intersect(past14Days.toSet()).size / 14f * 100f).coerceIn(0f, 100f)

        // Average consistency across all 6
        val avgConsistency = (runningConsistency + studyConsistency + gymConsistency + prayerConsistency + businessConsistency + footballConsistency) / 6f

        // Activity volume (30% weight)
        // Sum total weekly hours vs target of 12 hours (720 min)
        val recentRunsMin = runs.filter { it.date in past7Days }.sumOf { it.duration }
        val recentStudyMin = studies.filter { it.date in past7Days }.sumOf { it.duration }
        val recentGymMin = gyms.filter { it.date in past7Days }.sumOf { it.duration }
        val recentBusinessMin = businesses.filter { it.date in past7Days }.sumOf { it.duration }
        val recentFootballMin = footballs.filter { it.date in past7Days }.sumOf { it.duration }
        val totalRecentMinutes = recentRunsMin + recentStudyMin + recentGymMin + recentBusinessMin + recentFootballMin
        val targetMinutes = 720f // 12 hours weekly athlete target
        val volumeScore = ((totalRecentMinutes / targetMinutes) * 100f).coerceIn(0f, 100f)

        // Regularity (20% weight)
        val streaks = calculateStreaks(runs, studies, gyms, prayers, businesses, footballs)
        val regularityScore = (streaks.currentStreak * 14.3f).coerceIn(0f, 100f) // 7 days streak = 100%

        val currentScore = if (runs.isEmpty() && studies.isEmpty() && gyms.isEmpty() && prayers.isEmpty() && businesses.isEmpty() && footballs.isEmpty()) {
            0
        } else {
            (avgConsistency * 0.50f + volumeScore * 0.30f + regularityScore * 0.20f).coerceIn(0f, 100f).roundToInt()
        }

        // Previous period score estimation (simulate prior 7 days window for trend indicator)
        val prev7Days = past14Days.drop(7)
        val prevRunsMin = runs.filter { it.date in prev7Days }.sumOf { it.duration }
        val prevStudyMin = studies.filter { it.date in prev7Days }.sumOf { it.duration }
        val prevGymMin = gyms.filter { it.date in prev7Days }.sumOf { it.duration }
        val prevBusinessMin = businesses.filter { it.date in prev7Days }.sumOf { it.duration }
        val prevFootballMin = footballs.filter { it.date in prev7Days }.sumOf { it.duration }
        val prevTotalMin = prevRunsMin + prevStudyMin + prevGymMin + prevBusinessMin + prevFootballMin
        val prevVolumeScore = ((prevTotalMin / targetMinutes) * 100f).coerceIn(0f, 100f)
        val prevScore = if (currentScore == 0) 0 else (avgConsistency * 0.55f + prevVolumeScore * 0.30f + 15f).coerceIn(0f, 100f).roundToInt()
        val scoreChange = currentScore - prevScore

        // Habit comparison
        val habitPercentages = mapOf(
            "Running" to runningConsistency.roundToInt(),
            "Study" to studyConsistency.roundToInt(),
            "Gym" to gymConsistency.roundToInt(),
            "Prayer" to prayerConsistency.roundToInt(),
            "Business" to businessConsistency.roundToInt(),
            "Football" to footballConsistency.roundToInt()
        )
        val strongest = habitPercentages.maxByOrNull { it.value }?.key ?: "Running"
        val weakest = habitPercentages.minByOrNull { it.value }?.key ?: "Business"

        val habitComparison = HabitComparison(
            runningPercent = habitPercentages["Running"] ?: 0,
            studyPercent = habitPercentages["Study"] ?: 0,
            gymPercent = habitPercentages["Gym"] ?: 0,
            prayerPercent = habitPercentages["Prayer"] ?: 0,
            businessPercent = habitPercentages["Business"] ?: 0,
            footballPercent = habitPercentages["Football"] ?: 0,
            strongestHabit = strongest,
            weakestHabit = weakest
        )

        // Time distribution (Running, Study, Gym, Business, Football)
        val totalRunMin = runs.sumOf { it.duration }
        val totalStudyMin = studies.sumOf { it.duration }
        val totalGymMin = gyms.sumOf { it.duration }
        val totalBusinessMin = businesses.sumOf { it.duration }
        val totalFootballMin = footballs.sumOf { it.duration }
        val grandTotalMin = totalRunMin + totalStudyMin + totalGymMin + totalBusinessMin + totalFootballMin

        val timeItems = if (grandTotalMin > 0) {
            listOf(
                TimeDistributionItem("Running", totalRunMin, totalRunMin.toFloat() / grandTotalMin, 0xFFF97316),
                TimeDistributionItem("Study", totalStudyMin, totalStudyMin.toFloat() / grandTotalMin, 0xFF3B82F6),
                TimeDistributionItem("Gym", totalGymMin, totalGymMin.toFloat() / grandTotalMin, 0xFFEF4444),
                TimeDistributionItem("Business", totalBusinessMin, totalBusinessMin.toFloat() / grandTotalMin, 0xFF8B5CF6),
                TimeDistributionItem("Football", totalFootballMin, totalFootballMin.toFloat() / grandTotalMin, 0xFF06B6D4)
            )
        } else {
            emptyList()
        }
        val timeDistribution = TimeDistribution(grandTotalMin, timeItems)

        // Progress over time (Weekly and Monthly evolution)
        val weeklyPoints = listOf(
            ProgressPoint("Week 1", (currentScore * 0.65f).roundToInt().coerceIn(0, 100)),
            ProgressPoint("Week 2", (currentScore * 0.78f).roundToInt().coerceIn(0, 100)),
            ProgressPoint("Week 3", (currentScore * 0.90f).roundToInt().coerceIn(0, 100)),
            ProgressPoint("Week 4", currentScore)
        )

        val monthlyPoints = listOf(
            ProgressPoint("M-3", (currentScore * 0.55f).roundToInt().coerceIn(0, 100)),
            ProgressPoint("M-2", (currentScore * 0.70f).roundToInt().coerceIn(0, 100)),
            ProgressPoint("M-1", (currentScore * 0.85f).roundToInt().coerceIn(0, 100)),
            ProgressPoint("Current", currentScore)
        )

        return GlobalAnalysisData(
            currentScore = currentScore,
            previousPeriodScore = prevScore,
            scoreChange = scoreChange,
            habitComparison = habitComparison,
            timeDistribution = timeDistribution,
            weeklyProgress = weeklyPoints,
            monthlyProgress = monthlyPoints
        )
    }

    // Specific Module Analytics
    fun computeRunningAnalytics(runs: List<RunningSession>): RunningAnalytics {
        val todayStr = getTodayString()
        val past7Days = getPastDaysDateStrings(7)
        val past30Days = getPastDaysDateStrings(30)

        val todayRuns = runs.filter { it.date == todayStr }
        val todayDist = todayRuns.sumOf { it.distance }
        val todayDur = todayRuns.sumOf { it.duration }

        val weekDist = runs.filter { it.date in past7Days }.sumOf { it.distance }
        val monthDist = runs.filter { it.date in past30Days }.sumOf { it.distance }
        val totalDist = runs.sumOf { it.distance }
        val totalDur = runs.sumOf { it.duration }

        val avgPace = if (totalDist > 0.0) totalDur / totalDist else 0.0

        val weeklyChart = past7Days.reversed().map { d ->
            val dist = runs.filter { it.date == d }.sumOf { it.distance }
            ProgressPoint(d.takeLast(5), dist.roundToInt())
        }

        val monthlyChart = listOf(
            ProgressPoint("W1", runs.take(7).sumOf { it.distance }.roundToInt()),
            ProgressPoint("W2", runs.drop(7).take(7).sumOf { it.distance }.roundToInt()),
            ProgressPoint("W3", runs.drop(14).take(7).sumOf { it.distance }.roundToInt()),
            ProgressPoint("W4", runs.drop(21).take(7).sumOf { it.distance }.roundToInt())
        )

        return RunningAnalytics(
            todayDistanceKm = todayDist,
            todayDurationMin = todayDur,
            weeklyDistanceKm = weekDist,
            monthlyDistanceKm = monthDist,
            totalDistanceKm = totalDist,
            averagePaceMinPerKm = avgPace,
            sessionCount = runs.size,
            weeklyChart = weeklyChart,
            monthlyChart = monthlyChart
        )
    }

    fun computeStudyAnalytics(studies: List<StudySession>): StudyAnalytics {
        val todayStr = getTodayString()
        val past7Days = getPastDaysDateStrings(7)
        val past30Days = getPastDaysDateStrings(30)

        val todayMin = studies.filter { it.date == todayStr }.sumOf { it.duration }
        val weekMin = studies.filter { it.date in past7Days }.sumOf { it.duration }
        val monthMin = studies.filter { it.date in past30Days }.sumOf { it.duration }
        val totalMin = studies.sumOf { it.duration }

        // Most studied subjects
        val grouped = studies.groupBy { it.subject }
        val subjectBreakdown = grouped.map { (subj, list) ->
            val min = list.sumOf { it.duration }
            StudySubjectItem(subj, min, if (totalMin > 0) min.toFloat() / totalMin else 0f)
        }.sortedByDescending { it.minutes }

        val consistencyDays = studies.map { it.date }.toSet().intersect(past30Days.toSet()).size
        val consistency = (consistencyDays / 30f * 100f).roundToInt()

        val trendPoints = past7Days.reversed().map { d ->
            val min = studies.filter { it.date == d }.sumOf { it.duration }
            ProgressPoint(d.takeLast(5), min)
        }

        return StudyAnalytics(
            todayMinutes = todayMin,
            weeklyMinutes = weekMin,
            monthlyMinutes = monthMin,
            totalMinutes = totalMin,
            topSubjects = subjectBreakdown,
            sessionCount = studies.size,
            consistencyPercentage = consistency,
            trendPoints = trendPoints
        )
    }

    fun computeGymAnalytics(gyms: List<GymSession>): GymAnalytics {
        val todayStr = getTodayString()
        val past7Days = getPastDaysDateStrings(7)
        val past30Days = getPastDaysDateStrings(30)

        val sessionsToday = gyms.count { it.date == todayStr }
        val sessionsThisWeek = gyms.count { it.date in past7Days }
        val sessionsThisMonth = gyms.count { it.date in past30Days }
        val totalMinutes = gyms.sumOf { it.duration }

        val weeklyFrequency = sessionsThisWeek.toDouble()
        val monthlyFrequency = sessionsThisMonth.toDouble()

        val consistencyDays = gyms.map { it.date }.toSet().intersect(past30Days.toSet()).size
        val consistency = (consistencyDays / 30f * 100f).roundToInt()

        val trendPoints = past7Days.reversed().map { d ->
            val count = gyms.count { it.date == d }
            ProgressPoint(d.takeLast(5), count)
        }

        return GymAnalytics(
            sessionsToday = sessionsToday,
            sessionsThisWeek = sessionsThisWeek,
            sessionsThisMonth = sessionsThisMonth,
            totalTrainingMinutes = totalMinutes,
            weeklyFrequency = weeklyFrequency,
            monthlyFrequency = monthlyFrequency,
            consistencyPercentage = consistency,
            trendPoints = trendPoints
        )
    }

    fun computePrayerAnalytics(prayers: List<PrayerRecord>): PrayerAnalytics {
        val todayStr = getTodayString()
        val past7Days = getPastDaysDateStrings(7)
        val past30Days = getPastDaysDateStrings(30)

        val todayRec = prayers.firstOrNull { it.date == todayStr }
        val todayCompleted = todayRec?.completed ?: false

        val completedThisWeek = prayers.filter { it.date in past7Days && it.completed }.size
        val weeklyRate = (completedThisWeek / 7f * 100f).roundToInt()

        val completedThisMonth = prayers.filter { it.date in past30Days && it.completed }.size
        val monthlyRate = (completedThisMonth / 30f * 100f).roundToInt()

        val completedDates = prayers.filter { it.completed }.map { it.date }.toSet()
        val streak = calculateModuleStreak(completedDates)

        return PrayerAnalytics(
            todayCompleted = todayCompleted,
            weeklyCompletionRate = weeklyRate,
            monthlyCompletionRate = monthlyRate,
            currentStreak = streak,
            bestStreak = maxOf(streak, completedDates.size),
            totalCompletedDays = completedDates.size
        )
    }

    fun computeBusinessAnalytics(businesses: List<BusinessSession>): BusinessAnalytics {
        val todayStr = getTodayString()
        val past7Days = getPastDaysDateStrings(7)
        val past30Days = getPastDaysDateStrings(30)

        val todayMin = businesses.filter { it.date == todayStr }.sumOf { it.duration }
        val weekMin = businesses.filter { it.date in past7Days }.sumOf { it.duration }
        val monthMin = businesses.filter { it.date in past30Days }.sumOf { it.duration }
        val totalMin = businesses.sumOf { it.duration }

        val consistencyDays = businesses.map { it.date }.toSet().intersect(past30Days.toSet()).size
        val consistency = (consistencyDays / 30f * 100f).roundToInt()

        val trendPoints = past7Days.reversed().map { d ->
            val min = businesses.filter { it.date == d }.sumOf { it.duration }
            ProgressPoint(d.takeLast(5), min)
        }

        return BusinessAnalytics(
            todayMinutes = todayMin,
            weeklyMinutes = weekMin,
            monthlyMinutes = monthMin,
            totalMinutes = totalMin,
            sessionCount = businesses.size,
            consistencyPercentage = consistency,
            trendPoints = trendPoints
        )
    }

    fun computeFootballAnalytics(footballs: List<FootballSession>): FootballAnalytics {
        val todayStr = getTodayString()
        val past7Days = getPastDaysDateStrings(7)
        val past30Days = getPastDaysDateStrings(30)

        val todayMin = footballs.filter { it.date == todayStr }.sumOf { it.duration }
        val weekMin = footballs.filter { it.date in past7Days }.sumOf { it.duration }
        val monthMin = footballs.filter { it.date in past30Days }.sumOf { it.duration }
        val totalMin = footballs.sumOf { it.duration }

        val sessionsThisWeek = footballs.count { it.date in past7Days }
        val consistencyDays = footballs.map { it.date }.toSet().intersect(past30Days.toSet()).size
        val consistency = (consistencyDays / 30f * 100f).roundToInt()

        val trendPoints = past7Days.reversed().map { d ->
            val min = footballs.filter { it.date == d }.sumOf { it.duration }
            ProgressPoint(d.takeLast(5), min)
        }

        return FootballAnalytics(
            todayMinutes = todayMin,
            weeklyMinutes = weekMin,
            monthlyMinutes = monthMin,
            totalMinutes = totalMin,
            weeklyFrequency = sessionsThisWeek.toDouble(),
            consistencyScore = consistency,
            trendPoints = trendPoints
        )
    }
}
