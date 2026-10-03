package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BusinessSession
import com.example.data.entity.FootballSession
import com.example.data.entity.GymSession
import com.example.data.entity.PrayerRecord
import com.example.data.entity.RunningSession
import com.example.data.entity.StudySession
import com.example.data.repository.AthleteRepository
import com.example.data.repository.BusinessAnalytics
import com.example.data.repository.FootballAnalytics
import com.example.data.repository.GymAnalytics
import com.example.data.repository.PrayerAnalytics
import com.example.data.repository.RunningAnalytics
import com.example.data.repository.StudyAnalytics
import com.example.ui.components.InteractiveTrendLineChart
import com.example.ui.components.SubjectBarChart
import com.example.ui.components.getModuleColor
import com.example.ui.components.getModuleIcon
import com.example.ui.theme.VoltGreen
import com.example.ui.viewmodel.ModuleType
import java.util.Locale
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    moduleType: ModuleType,
    onBack: () -> Unit,
    onQuickLog: (ModuleType) -> Unit,
    // Running data
    runningAnalytics: RunningAnalytics,
    runningSessions: List<RunningSession>,
    onDeleteRun: (Long) -> Unit,
    // Study data
    studyAnalytics: StudyAnalytics,
    studySessions: List<StudySession>,
    onDeleteStudy: (Long) -> Unit,
    // Gym data
    gymAnalytics: GymAnalytics,
    gymSessions: List<GymSession>,
    onDeleteGym: (Long) -> Unit,
    // Prayer data
    prayerAnalytics: PrayerAnalytics,
    prayerRecords: List<PrayerRecord>,
    onTogglePrayer: (Boolean, String) -> Unit,
    // Business data
    businessAnalytics: BusinessAnalytics,
    businessSessions: List<BusinessSession>,
    onDeleteBusiness: (Long) -> Unit,
    // Football data
    footballAnalytics: FootballAnalytics,
    footballSessions: List<FootballSession>,
    onDeleteFootball: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val modColor = getModuleColor(moduleType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(modColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getModuleIcon(moduleType),
                                contentDescription = moduleType.title,
                                tint = modColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = moduleType.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { onQuickLog(moduleType) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("detail_log_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = modColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("module_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Analytics & Content according to module type
            when (moduleType) {
                ModuleType.RUNNING -> {
                    item {
                        RunningAnalyticsContent(runningAnalytics, modColor)
                    }
                    item {
                        Text(
                            text = "SESSION HISTORY (${runningSessions.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (runningSessions.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No running sessions yet. Start your first session.",
                                onAction = { onQuickLog(ModuleType.RUNNING) },
                                actionText = "Log Run"
                            )
                        }
                    } else {
                        items(runningSessions, key = { it.id }) { run ->
                            val pace = if (run.distance > 0) run.duration / run.distance else 0.0
                            val paceMins = floor(pace).toInt()
                            val paceSecs = ((pace - paceMins) * 60).toInt()
                            val formattedPace = String.format(Locale.US, "%d:%02d min/km", paceMins, paceSecs)

                            HistoryRowCard(
                                title = String.format(Locale.US, "%.2f km", run.distance),
                                subtitle = "${run.duration} mins • $formattedPace",
                                date = run.date,
                                onDelete = { onDeleteRun(run.id) },
                                accentColor = modColor
                            )
                        }
                    }
                }

                ModuleType.STUDY -> {
                    item {
                        StudyAnalyticsContent(studyAnalytics, modColor)
                    }
                    item {
                        Text(
                            text = "SESSION HISTORY (${studySessions.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (studySessions.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "Start your first study session.",
                                onAction = { onQuickLog(ModuleType.STUDY) },
                                actionText = "Log Study"
                            )
                        }
                    } else {
                        items(studySessions, key = { it.id }) { s ->
                            HistoryRowCard(
                                title = s.subject,
                                subtitle = "${s.duration} minutes",
                                date = s.date,
                                onDelete = { onDeleteStudy(s.id) },
                                accentColor = modColor
                            )
                        }
                    }
                }

                ModuleType.GYM -> {
                    item {
                        GymAnalyticsContent(gymAnalytics, modColor)
                    }
                    item {
                        Text(
                            text = "SESSION HISTORY (${gymSessions.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (gymSessions.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No gym sessions yet. Start your first workout.",
                                onAction = { onQuickLog(ModuleType.GYM) },
                                actionText = "Log Gym"
                            )
                        }
                    } else {
                        items(gymSessions, key = { it.id }) { g ->
                            HistoryRowCard(
                                title = "Strength & Conditioning",
                                subtitle = "${g.duration} minutes workout",
                                date = g.date,
                                onDelete = { onDeleteGym(g.id) },
                                accentColor = modColor
                            )
                        }
                    }
                }

                ModuleType.PRAYER -> {
                    item {
                        PrayerAnalyticsContent(
                            prayerAnalytics,
                            modColor,
                            onToggleToday = {
                                val todayStr = AthleteRepository.getTodayString()
                                onTogglePrayer(!prayerAnalytics.todayCompleted, todayStr)
                            }
                        )
                    }
                    item {
                        Text(
                            text = "PRAYER LOG (${prayerRecords.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (prayerRecords.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No prayer records yet. Mark today completed.",
                                onAction = {
                                    val todayStr = AthleteRepository.getTodayString()
                                    onTogglePrayer(true, todayStr)
                                },
                                actionText = "Mark Today Done"
                            )
                        }
                    } else {
                        items(prayerRecords, key = { it.id }) { p ->
                            HistoryRowCard(
                                title = if (p.completed) "Prayer Completed" else "Prayer Missed",
                                subtitle = if (p.completed) "Discipline & spiritual focus verified" else "Pending completion",
                                date = p.date,
                                onDelete = null,
                                accentColor = if (p.completed) modColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                ModuleType.BUSINESS -> {
                    item {
                        BusinessAnalyticsContent(businessAnalytics, modColor)
                    }
                    item {
                        Text(
                            text = "SESSION HISTORY (${businessSessions.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (businessSessions.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "Start your first business development session.",
                                onAction = { onQuickLog(ModuleType.BUSINESS) },
                                actionText = "Log Business"
                            )
                        }
                    } else {
                        items(businessSessions, key = { it.id }) { b ->
                            HistoryRowCard(
                                title = "Business Building",
                                subtitle = "${b.duration} minutes invested",
                                date = b.date,
                                onDelete = { onDeleteBusiness(b.id) },
                                accentColor = modColor
                            )
                        }
                    }
                }

                ModuleType.FOOTBALL -> {
                    item {
                        FootballAnalyticsContent(footballAnalytics, modColor)
                    }
                    item {
                        Text(
                            text = "SESSION HISTORY (${footballSessions.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (footballSessions.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No football training recorded yet.",
                                onAction = { onQuickLog(ModuleType.FOOTBALL) },
                                actionText = "Log Training"
                            )
                        }
                    } else {
                        items(footballSessions, key = { it.id }) { f ->
                            HistoryRowCard(
                                title = "On-Pitch Training & Drills",
                                subtitle = "${f.duration} minutes tactical/physical session",
                                date = f.date,
                                onDelete = { onDeleteFootball(f.id) },
                                accentColor = modColor
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// Module Analytics Widgets
// -------------------------------------------------------------

@Composable
fun RunningAnalyticsContent(analytics: RunningAnalytics, color: Color) {
    val paceMins = floor(analytics.averagePaceMinPerKm).toInt()
    val paceSecs = ((analytics.averagePaceMinPerKm - paceMins) * 60).toInt()
    val formattedPace = if (analytics.averagePaceMinPerKm > 0) String.format(Locale.US, "%d:%02d", paceMins, paceSecs) else "--:--"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Metric Cards Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Today", String.format(Locale.US, "%.1f km", analytics.todayDistanceKm), "${analytics.todayDurationMin} min", color, Modifier.weight(1f))
            MetricStatCard("Avg Pace", "$formattedPace min/km", "${analytics.sessionCount} runs", color, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("This Week", String.format(Locale.US, "%.1f km", analytics.weeklyDistanceKm), "last 7 days", color, Modifier.weight(1f))
            MetricStatCard("This Month", String.format(Locale.US, "%.1f km", analytics.monthlyDistanceKm), "Total ${String.format(Locale.US, "%.1f", analytics.totalDistanceKm)} km", color, Modifier.weight(1f))
        }

        // Chart Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DISTANCE OVER TIME (KM)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendLineChart(points = analytics.weeklyChart, lineColor = color, unitSuffix = "km")
            }
        }
    }
}

@Composable
fun StudyAnalyticsContent(analytics: StudyAnalytics, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Today", "${analytics.todayMinutes}m", "${analytics.sessionCount} sessions", color, Modifier.weight(1f))
            MetricStatCard("Consistency", "${analytics.consistencyPercentage}%", "past 30 days", color, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("This Week", String.format(Locale.US, "%.1fh", analytics.weeklyMinutes / 60.0), "${analytics.weeklyMinutes}m total", color, Modifier.weight(1f))
            MetricStatCard("This Month", String.format(Locale.US, "%.1fh", analytics.monthlyMinutes / 60.0), "All-time: ${String.format(Locale.US, "%.1fh", analytics.totalMinutes / 60.0)}", color, Modifier.weight(1f))
        }

        // Subject Breakdown Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MOST STUDIED SUBJECTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                SubjectBarChart(subjects = analytics.topSubjects)
            }
        }

        // Trend Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STUDY-TIME TREND (MIN)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendLineChart(points = analytics.trendPoints, lineColor = color, unitSuffix = "m")
            }
        }
    }
}

@Composable
fun GymAnalyticsContent(analytics: GymAnalytics, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Sessions Today", "${analytics.sessionsToday}", "workouts", color, Modifier.weight(1f))
            MetricStatCard("Consistency", "${analytics.consistencyPercentage}%", "past 30 days", color, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("This Week", "${analytics.sessionsThisWeek}", "Freq: ${String.format(Locale.US, "%.1f/wk", analytics.weeklyFrequency)}", color, Modifier.weight(1f))
            MetricStatCard("This Month", "${analytics.sessionsThisMonth}", "Total: ${String.format(Locale.US, "%.1fh", analytics.totalTrainingMinutes / 60.0)}", color, Modifier.weight(1f))
        }

        // Trend Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRAINING TREND (SESSIONS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendLineChart(points = analytics.trendPoints, lineColor = color, unitSuffix = "")
            }
        }
    }
}

@Composable
fun PrayerAnalyticsContent(
    analytics: PrayerAnalytics,
    color: Color,
    onToggleToday: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Today Checkbox Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = analytics.todayCompleted,
                        onCheckedChange = { onToggleToday() },
                        colors = CheckboxDefaults.colors(checkedColor = color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (analytics.todayCompleted) "Today Completed ☑" else "Today Pending ☐",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to toggle today's prayer verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Current Streak", "${analytics.currentStreak} Days", "🔥 consecutive", color, Modifier.weight(1f))
            MetricStatCard("Best Streak", "${analytics.bestStreak} Days", "all-time record", color, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Weekly Rate", "${analytics.weeklyCompletionRate}%", "last 7 days", color, Modifier.weight(1f))
            MetricStatCard("Monthly Rate", "${analytics.monthlyCompletionRate}%", "Total: ${analytics.totalCompletedDays} days", color, Modifier.weight(1f))
        }
    }
}

@Composable
fun BusinessAnalyticsContent(analytics: BusinessAnalytics, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Today", "${analytics.todayMinutes}m", "${analytics.sessionCount} sessions", color, Modifier.weight(1f))
            MetricStatCard("Consistency", "${analytics.consistencyPercentage}%", "productivity score", color, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Weekly", String.format(Locale.US, "%.1fh", analytics.weeklyMinutes / 60.0), "${analytics.weeklyMinutes}m total", color, Modifier.weight(1f))
            MetricStatCard("Monthly", String.format(Locale.US, "%.1fh", analytics.monthlyMinutes / 60.0), "Invested: ${String.format(Locale.US, "%.1fh", analytics.totalMinutes / 60.0)}", color, Modifier.weight(1f))
        }

        // Trend Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PRODUCTIVITY TREND (MINUTES)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendLineChart(points = analytics.trendPoints, lineColor = color, unitSuffix = "m")
            }
        }
    }
}

@Composable
fun FootballAnalyticsContent(analytics: FootballAnalytics, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Today", "${analytics.todayMinutes}m", "pitch session", color, Modifier.weight(1f))
            MetricStatCard("Consistency", "${analytics.consistencyScore}%", "training consistency", color, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricStatCard("Weekly Training", String.format(Locale.US, "%.1fh", analytics.weeklyMinutes / 60.0), "Freq: ${String.format(Locale.US, "%.1f/wk", analytics.weeklyFrequency)}", color, Modifier.weight(1f))
            MetricStatCard("Monthly Training", String.format(Locale.US, "%.1fh", analytics.monthlyMinutes / 60.0), "Total: ${String.format(Locale.US, "%.1fh", analytics.totalMinutes / 60.0)}", color, Modifier.weight(1f))
        }

        // Trend Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FOOTBALL TRAINING TREND (MINUTES)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendLineChart(points = analytics.trendPoints, lineColor = color, unitSuffix = "m")
            }
        }
    }
}

@Composable
fun MetricStatCard(
    label: String,
    value: String,
    caption: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
fun HistoryRowCard(
    title: String,
    subtitle: String,
    date: String,
    onDelete: (() -> Unit)?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$subtitle • $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    onAction: () -> Unit,
    actionText: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
