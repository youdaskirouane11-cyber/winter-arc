package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.BusinessSession
import com.example.data.entity.FootballSession
import com.example.data.entity.GymSession
import com.example.data.entity.PrayerRecord
import com.example.data.entity.RunningSession
import com.example.data.entity.StudySession
import com.example.data.entity.UserProfile
import com.example.data.repository.AthleteLevel
import com.example.data.repository.AthleteRepository
import com.example.data.repository.BusinessAnalytics
import com.example.data.repository.FootballAnalytics
import com.example.data.repository.GlobalAnalysisData
import com.example.data.repository.GymAnalytics
import com.example.data.repository.PrayerAnalytics
import com.example.data.repository.RunningAnalytics
import com.example.data.repository.StreakInfo
import com.example.data.repository.StudyAnalytics
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class MainTab {
    HOME,
    ANALYSIS,
    SETTINGS
}

enum class ModuleType(val title: String, val unitLabel: String) {
    RUNNING("Running", "km"),
    STUDY("Study", "min"),
    GYM("Gym", "min"),
    PRAYER("Prayer", "status"),
    BUSINESS("Business", "min"),
    FOOTBALL("Football Training", "min")
}

data class ModuleCardState(
    val type: ModuleType,
    val title: String,
    val todayDisplay: String,
    val progressRatio: Float, // 0f to 1f
    val streakDays: Int,
    val isCompletedToday: Boolean
)

data class QuickInputState(
    val isOpen: Boolean = false,
    val module: ModuleType? = null
)

class AthleteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AthleteRepository(db)

    // Current navigation state
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeDetailModule = MutableStateFlow<ModuleType?>(null)
    val activeDetailModule: StateFlow<ModuleType?> = _activeDetailModule.asStateFlow()

    // Quick Input modal state
    private val _quickInputState = MutableStateFlow(QuickInputState())
    val quickInputState: StateFlow<QuickInputState> = _quickInputState.asStateFlow()

    // Notification toast / snackbar events
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserExists()
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalXp: StateFlow<Int> = repository.totalXp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val athleteLevel: StateFlow<AthleteLevel> = totalXp
        .map { xp ->
            repository.calculateLevel(xp)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.calculateLevel(0))

    val runningSessions: StateFlow<List<RunningSession>> = repository.runningSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySession>> = repository.studySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gymSessions: StateFlow<List<GymSession>> = repository.gymSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prayerRecords: StateFlow<List<PrayerRecord>> = repository.prayerRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessSessions: StateFlow<List<BusinessSession>> = repository.businessSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val footballSessions: StateFlow<List<FootballSession>> = repository.footballSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall Streak Info
    val streakInfo: StateFlow<StreakInfo> = combine(
        runningSessions,
        studySessions,
        gymSessions,
        prayerRecords,
        businessSessions,
        footballSessions
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        repository.calculateStreaks(
            args[0] as List<RunningSession>,
            args[1] as List<StudySession>,
            args[2] as List<GymSession>,
            args[3] as List<PrayerRecord>,
            args[4] as List<BusinessSession>,
            args[5] as List<FootballSession>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakInfo(0, 0))

    // Global Analysis Stream
    val globalAnalysis: StateFlow<GlobalAnalysisData> = repository.globalAnalysisData
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            repository.computeGlobalAnalysis(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        )

    // Module Analytics
    val runningAnalytics: StateFlow<RunningAnalytics> = runningSessions.map { r ->
        repository.computeRunningAnalytics(r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.computeRunningAnalytics(emptyList()))

    val studyAnalytics: StateFlow<StudyAnalytics> = studySessions.map { s ->
        repository.computeStudyAnalytics(s)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.computeStudyAnalytics(emptyList()))

    val gymAnalytics: StateFlow<GymAnalytics> = gymSessions.map { g ->
        repository.computeGymAnalytics(g)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.computeGymAnalytics(emptyList()))

    val prayerAnalytics: StateFlow<PrayerAnalytics> = prayerRecords.map { p ->
        repository.computePrayerAnalytics(p)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.computePrayerAnalytics(emptyList()))

    val businessAnalytics: StateFlow<BusinessAnalytics> = businessSessions.map { b ->
        repository.computeBusinessAnalytics(b)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.computeBusinessAnalytics(emptyList()))

    val footballAnalytics: StateFlow<FootballAnalytics> = footballSessions.map { f ->
        repository.computeFootballAnalytics(f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.computeFootballAnalytics(emptyList()))

    // Home 6-Module Grid Cards
    val moduleCards: StateFlow<List<ModuleCardState>> = combine(
        runningSessions,
        studySessions,
        gymSessions,
        prayerRecords,
        businessSessions,
        footballSessions
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        val runs = args[0] as List<RunningSession>
        @Suppress("UNCHECKED_CAST")
        val studies = args[1] as List<StudySession>
        @Suppress("UNCHECKED_CAST")
        val gyms = args[2] as List<GymSession>
        @Suppress("UNCHECKED_CAST")
        val prayers = args[3] as List<PrayerRecord>
        @Suppress("UNCHECKED_CAST")
        val businesses = args[4] as List<BusinessSession>
        @Suppress("UNCHECKED_CAST")
        val footballs = args[5] as List<FootballSession>

        val todayStr = AthleteRepository.getTodayString()

        // 1. Running
        val todayRuns = runs.filter { it.date == todayStr }
        val runKm = todayRuns.sumOf { it.distance }
        val runRatio = (runKm / 5.0).toFloat().coerceIn(0f, 1f) // Target 5 km
        val runStreak = runs.map { it.date }.toSet().size

        // 2. Study
        val todayStudyMin = studies.filter { it.date == todayStr }.sumOf { it.duration }
        val studyRatio = (todayStudyMin / 60f).coerceIn(0f, 1f) // Target 60 min
        val studyStreak = studies.map { it.date }.toSet().size

        // 3. Gym
        val todayGymCount = gyms.count { it.date == todayStr }
        val gymRatio = if (todayGymCount > 0) 1f else 0f
        val gymStreak = gyms.map { it.date }.toSet().size

        // 4. Prayer
        val prayerDone = prayers.any { it.date == todayStr && it.completed }
        val prayerRatio = if (prayerDone) 1f else 0f
        val prayerStreak = prayers.filter { it.completed }.map { it.date }.toSet().size

        // 5. Business
        val todayBizMin = businesses.filter { it.date == todayStr }.sumOf { it.duration }
        val bizRatio = (todayBizMin / 45f).coerceIn(0f, 1f) // Target 45 min
        val bizStreak = businesses.map { it.date }.toSet().size

        // 6. Football
        val todayFootMin = footballs.filter { it.date == todayStr }.sumOf { it.duration }
        val footRatio = (todayFootMin / 60f).coerceIn(0f, 1f) // Target 60 min
        val footStreak = footballs.map { it.date }.toSet().size

        listOf(
            ModuleCardState(
                type = ModuleType.RUNNING,
                title = "Running",
                todayDisplay = if (runKm > 0.0) String.format(java.util.Locale.US, "%.1f km", runKm) else "0.0 km",
                progressRatio = runRatio,
                streakDays = runStreak,
                isCompletedToday = runKm > 0.0
            ),
            ModuleCardState(
                type = ModuleType.STUDY,
                title = "Study",
                todayDisplay = if (todayStudyMin > 0) "${todayStudyMin}m" else "0m",
                progressRatio = studyRatio,
                streakDays = studyStreak,
                isCompletedToday = todayStudyMin > 0
            ),
            ModuleCardState(
                type = ModuleType.GYM,
                title = "Gym",
                todayDisplay = if (todayGymCount > 0) "$todayGymCount session" else "Not logged",
                progressRatio = gymRatio,
                streakDays = gymStreak,
                isCompletedToday = todayGymCount > 0
            ),
            ModuleCardState(
                type = ModuleType.PRAYER,
                title = "Prayer",
                todayDisplay = if (prayerDone) "Completed" else "Pending",
                progressRatio = prayerRatio,
                streakDays = prayerStreak,
                isCompletedToday = prayerDone
            ),
            ModuleCardState(
                type = ModuleType.BUSINESS,
                title = "Business",
                todayDisplay = if (todayBizMin > 0) "${todayBizMin}m" else "0m",
                progressRatio = bizRatio,
                streakDays = bizStreak,
                isCompletedToday = todayBizMin > 0
            ),
            ModuleCardState(
                type = ModuleType.FOOTBALL,
                title = "Football",
                todayDisplay = if (todayFootMin > 0) "${todayFootMin}m" else "0m",
                progressRatio = footRatio,
                streakDays = footStreak,
                isCompletedToday = todayFootMin > 0
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToTab(tab: MainTab) {
        _activeDetailModule.value = null
        _currentTab.value = tab
    }

    fun openModuleDetail(type: ModuleType) {
        _activeDetailModule.value = type
    }

    fun closeModuleDetail() {
        _activeDetailModule.value = null
    }

    fun openQuickInput(type: ModuleType? = null) {
        _quickInputState.value = QuickInputState(isOpen = true, module = type)
    }

    fun closeQuickInput() {
        _quickInputState.value = QuickInputState(isOpen = false, module = null)
    }

    // Logging functions
    fun logRunning(durationMin: Int, distanceKm: Double, date: String) {
        viewModelScope.launch {
            repository.addRunningSession(durationMin, distanceKm, date)
            _toastEvent.emit("✓ Running session saved! +10 XP")
            closeQuickInput()
        }
    }

    fun deleteRunningSession(id: Long) {
        viewModelScope.launch {
            repository.deleteRunningSession(id)
            _toastEvent.emit("Session deleted")
        }
    }

    fun logStudy(durationMin: Int, subject: String, date: String) {
        viewModelScope.launch {
            repository.addStudySession(durationMin, subject, date)
            _toastEvent.emit("✓ Study session saved! +10 XP")
            closeQuickInput()
        }
    }

    fun deleteStudySession(id: Long) {
        viewModelScope.launch {
            repository.deleteStudySession(id)
            _toastEvent.emit("Session deleted")
        }
    }

    fun logGym(durationMin: Int, date: String) {
        viewModelScope.launch {
            repository.addGymSession(durationMin, date)
            _toastEvent.emit("✓ Gym workout saved! +10 XP")
            closeQuickInput()
        }
    }

    fun deleteGymSession(id: Long) {
        viewModelScope.launch {
            repository.deleteGymSession(id)
            _toastEvent.emit("Session deleted")
        }
    }

    fun togglePrayer(completed: Boolean, date: String) {
        viewModelScope.launch {
            repository.togglePrayer(completed, date)
            if (completed) {
                _toastEvent.emit("✓ Prayer marked completed! +10 XP")
            } else {
                _toastEvent.emit("Prayer marked pending")
            }
            closeQuickInput()
        }
    }

    fun logBusiness(durationMin: Int, date: String) {
        viewModelScope.launch {
            repository.addBusinessSession(durationMin, date)
            _toastEvent.emit("✓ Business session saved! +10 XP")
            closeQuickInput()
        }
    }

    fun deleteBusinessSession(id: Long) {
        viewModelScope.launch {
            repository.deleteBusinessSession(id)
            _toastEvent.emit("Session deleted")
        }
    }

    fun logFootball(durationMin: Int, date: String) {
        viewModelScope.launch {
            repository.addFootballSession(durationMin, date)
            _toastEvent.emit("✓ Football training saved! +10 XP")
            closeQuickInput()
        }
    }

    fun deleteFootballSession(id: Long) {
        viewModelScope.launch {
            repository.deleteFootballSession(id)
            _toastEvent.emit("Session deleted")
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _toastEvent.emit("Sample athlete data loaded!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _toastEvent.emit("All data reset")
        }
    }

    fun updateProfile(name: String, email: String, isDarkMode: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateUser(current.copy(name = name.trim().ifEmpty { "Alex Stone" }, email = email, isDarkMode = isDarkMode))
            _toastEvent.emit("Profile updated")
        }
    }
}
