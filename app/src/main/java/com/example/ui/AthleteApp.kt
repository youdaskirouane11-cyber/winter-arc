package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.QuickInputBottomSheet
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModuleDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AthleteViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ModuleType
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AthleteApp(
    viewModel: AthleteViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeDetailModule by viewModel.activeDetailModule.collectAsStateWithLifecycle()
    val quickInputState by viewModel.quickInputState.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val athleteLevel by viewModel.athleteLevel.collectAsStateWithLifecycle()
    val streakInfo by viewModel.streakInfo.collectAsStateWithLifecycle()
    val globalAnalysis by viewModel.globalAnalysis.collectAsStateWithLifecycle()
    val moduleCards by viewModel.moduleCards.collectAsStateWithLifecycle()

    // Analytics
    val runningAnalytics by viewModel.runningAnalytics.collectAsStateWithLifecycle()
    val runningSessions by viewModel.runningSessions.collectAsStateWithLifecycle()

    val studyAnalytics by viewModel.studyAnalytics.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()

    val gymAnalytics by viewModel.gymAnalytics.collectAsStateWithLifecycle()
    val gymSessions by viewModel.gymSessions.collectAsStateWithLifecycle()

    val prayerAnalytics by viewModel.prayerAnalytics.collectAsStateWithLifecycle()
    val prayerRecords by viewModel.prayerRecords.collectAsStateWithLifecycle()

    val businessAnalytics by viewModel.businessAnalytics.collectAsStateWithLifecycle()
    val businessSessions by viewModel.businessSessions.collectAsStateWithLifecycle()

    val footballAnalytics by viewModel.footballAnalytics.collectAsStateWithLifecycle()
    val footballSessions by viewModel.footballSessions.collectAsStateWithLifecycle()

    // Theme state
    var isDarkMode by remember { mutableStateOf(true) }
    LaunchedEffect(userProfile) {
        userProfile?.let { isDarkMode = it.isDarkMode }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.toastEvent) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    MyApplicationTheme(darkTheme = isDarkMode) {
        // If a module detail screen is open, show it
        if (activeDetailModule != null) {
            ModuleDetailScreen(
                moduleType = activeDetailModule!!,
                onBack = { viewModel.closeModuleDetail() },
                onQuickLog = { mod -> viewModel.openQuickInput(mod) },
                runningAnalytics = runningAnalytics,
                runningSessions = runningSessions,
                onDeleteRun = { id -> viewModel.deleteRunningSession(id) },
                studyAnalytics = studyAnalytics,
                studySessions = studySessions,
                onDeleteStudy = { id -> viewModel.deleteStudySession(id) },
                gymAnalytics = gymAnalytics,
                gymSessions = gymSessions,
                onDeleteGym = { id -> viewModel.deleteGymSession(id) },
                prayerAnalytics = prayerAnalytics,
                prayerRecords = prayerRecords,
                onTogglePrayer = { done, date -> viewModel.togglePrayer(done, date) },
                businessAnalytics = businessAnalytics,
                businessSessions = businessSessions,
                onDeleteBusiness = { id -> viewModel.deleteBusinessSession(id) },
                footballAnalytics = footballAnalytics,
                footballSessions = footballSessions,
                onDeleteFootball = { id -> viewModel.deleteFootballSession(id) }
            )
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.HOME,
                            onClick = { viewModel.navigateToTab(MainTab.HOME) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GoldPrimary.copy(alpha = 0.2f),
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary
                            ),
                            modifier = Modifier.testTag("nav_tab_home")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.ANALYSIS,
                            onClick = { viewModel.navigateToTab(MainTab.ANALYSIS) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainTab.ANALYSIS) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                                    contentDescription = "Analysis"
                                )
                            },
                            label = { Text("Analysis", fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GoldPrimary.copy(alpha = 0.2f),
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary
                            ),
                            modifier = Modifier.testTag("nav_tab_analysis")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.SETTINGS,
                            onClick = { viewModel.navigateToTab(MainTab.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings", fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GoldPrimary.copy(alpha = 0.2f),
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary
                            ),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                },
                floatingActionButton = {
                    if (currentTab != MainTab.SETTINGS) {
                        FloatingActionButton(
                            onClick = { viewModel.openQuickInput(null) },
                            containerColor = GoldPrimary,
                            contentColor = Color.Black,
                            modifier = Modifier
                                .testTag("fab_quick_log")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Quick Log Session",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                userProfile = userProfile,
                                level = athleteLevel,
                                streakInfo = streakInfo,
                                globalAnalysis = globalAnalysis,
                                cards = moduleCards,
                                onOpenAnalysis = { viewModel.navigateToTab(MainTab.ANALYSIS) },
                                onOpenModuleDetail = { mod -> viewModel.openModuleDetail(mod) },
                                onQuickLog = { mod -> viewModel.openQuickInput(mod) }
                            )
                        }

                        MainTab.ANALYSIS -> {
                            AnalysisScreen(
                                analysisData = globalAnalysis
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                userProfile = userProfile,
                                level = athleteLevel,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = {
                                    isDarkMode = it
                                    viewModel.updateProfile(userProfile?.name ?: "Young Athlete", userProfile?.email ?: "", it)
                                },
                                onUpdateProfile = { name, email, dark ->
                                    viewModel.updateProfile(name, email, dark)
                                },
                                onSeedSampleData = { viewModel.seedSampleData() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }
        }

        if (quickInputState.isOpen) {
            QuickInputBottomSheet(
                initialModule = quickInputState.module,
                onDismiss = { viewModel.closeQuickInput() },
                onSaveRunning = { dur, dist, date -> viewModel.logRunning(dur, dist, date) },
                onSaveStudy = { dur, subj, date -> viewModel.logStudy(dur, subj, date) },
                onSaveGym = { dur, date -> viewModel.logGym(dur, date) },
                onSavePrayer = { done, date -> viewModel.togglePrayer(done, date) },
                onSaveBusiness = { dur, date -> viewModel.logBusiness(dur, date) },
                onSaveFootball = { dur, date -> viewModel.logFootball(dur, date) }
            )
        }
    }
}
