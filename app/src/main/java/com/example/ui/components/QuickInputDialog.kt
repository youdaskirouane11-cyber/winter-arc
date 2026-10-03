package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AthleteRepository
import com.example.ui.theme.ColorBusiness
import com.example.ui.theme.ColorFootball
import com.example.ui.theme.ColorGym
import com.example.ui.theme.ColorPrayer
import com.example.ui.theme.ColorRunning
import com.example.ui.theme.ColorStudy
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.ModuleType

fun getModuleIcon(type: ModuleType): ImageVector {
    return when (type) {
        ModuleType.RUNNING -> Icons.Default.DirectionsRun
        ModuleType.STUDY -> Icons.Default.MenuBook
        ModuleType.GYM -> Icons.Default.FitnessCenter
        ModuleType.PRAYER -> Icons.Default.SelfImprovement
        ModuleType.BUSINESS -> Icons.Default.BusinessCenter
        ModuleType.FOOTBALL -> Icons.Default.SportsSoccer
    }
}

fun getModuleColor(type: ModuleType): Color {
    return when (type) {
        ModuleType.RUNNING -> ColorRunning
        ModuleType.STUDY -> ColorStudy
        ModuleType.GYM -> ColorGym
        ModuleType.PRAYER -> ColorPrayer
        ModuleType.BUSINESS -> ColorBusiness
        ModuleType.FOOTBALL -> ColorFootball
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickInputBottomSheet(
    initialModule: ModuleType?,
    onDismiss: () -> Unit,
    onSaveRunning: (duration: Int, distance: Double, date: String) -> Unit,
    onSaveStudy: (duration: Int, subject: String, date: String) -> Unit,
    onSaveGym: (duration: Int, date: String) -> Unit,
    onSavePrayer: (completed: Boolean, date: String) -> Unit,
    onSaveBusiness: (duration: Int, date: String) -> Unit,
    onSaveFootball: (duration: Int, date: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedModule by remember { mutableStateOf(initialModule) }

    // Common Inputs
    var dateString by remember { mutableStateOf(AthleteRepository.getTodayString()) }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var distanceKm by remember { mutableDoubleStateOf(5.0) }
    var studySubject by remember { mutableStateOf("Tactics & Strategy") }
    var prayerCompleted by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_input_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedModule == null) "QUICK LOG" else "LOG ${selectedModule?.title?.uppercase()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Earn +10 XP per session",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_quick_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedModule == null) {
                // Step 1: Select Module
                Text(
                    text = "Select an athlete module:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(ModuleType.values()) { module ->
                        val modColor = getModuleColor(module)
                        Card(
                            onClick = { selectedModule = module },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("select_module_${module.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(modColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getModuleIcon(module),
                                        contentDescription = module.title,
                                        tint = modColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = module.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                val currentMod = selectedModule!!
                val modColor = getModuleColor(currentMod)

                // Date Field (editable)
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = modColor
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Module-Specific Form Inputs
                when (currentMod) {
                    ModuleType.RUNNING -> {
                        // Distance
                        Text(
                            text = "Distance: ${String.format(java.util.Locale.US, "%.1f", distanceKm)} km",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(3.0, 5.0, 7.5, 10.0).forEach { dist ->
                                FilterChip(
                                    selected = distanceKm == dist,
                                    onClick = { distanceKm = dist },
                                    label = { Text("${dist.toInt()}k") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        OutlinedTextField(
                            value = distanceKm.toString(),
                            onValueChange = { distanceKm = it.toDoubleOrNull() ?: distanceKm },
                            label = { Text("Custom Distance (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("distance_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Duration
                        Text(
                            text = "Duration: $durationMinutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(20, 30, 45, 60).forEach { min ->
                                FilterChip(
                                    selected = durationMinutes == min,
                                    onClick = { durationMinutes = min },
                                    label = { Text("${min}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ModuleType.STUDY -> {
                        // Subject
                        Text(
                            text = "Subject",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Tactics", "Physiology", "Business", "Mental").forEach { subj ->
                                FilterChip(
                                    selected = studySubject == subj,
                                    onClick = { studySubject = subj },
                                    label = { Text(subj) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        OutlinedTextField(
                            value = studySubject,
                            onValueChange = { studySubject = it },
                            label = { Text("Subject Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("study_subject_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Duration
                        Text(
                            text = "Study Duration: $durationMinutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(25, 45, 60, 90).forEach { min ->
                                FilterChip(
                                    selected = durationMinutes == min,
                                    onClick = { durationMinutes = min },
                                    label = { Text("${min}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ModuleType.GYM -> {
                        Text(
                            text = "Gym Workout Duration: $durationMinutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 45, 60, 75).forEach { min ->
                                FilterChip(
                                    selected = durationMinutes == min,
                                    onClick = { durationMinutes = min },
                                    label = { Text("${min}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ModuleType.PRAYER -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { prayerCompleted = !prayerCompleted }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = prayerCompleted,
                                    onCheckedChange = { prayerCompleted = it },
                                    colors = CheckboxDefaults.colors(checkedColor = modColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (prayerCompleted) "Prayer Completed" else "Not completed",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Daily spiritual discipline & reflection",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    ModuleType.BUSINESS -> {
                        Text(
                            text = "Business Session Duration: $durationMinutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 45, 60, 90).forEach { min ->
                                FilterChip(
                                    selected = durationMinutes == min,
                                    onClick = { durationMinutes = min },
                                    label = { Text("${min}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    ModuleType.FOOTBALL -> {
                        Text(
                            text = "Football Training Duration: $durationMinutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(45, 60, 90, 120).forEach { min ->
                                FilterChip(
                                    selected = durationMinutes == min,
                                    onClick = { durationMinutes = min },
                                    label = { Text("${min}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = modColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Action Button
                Button(
                    onClick = {
                        when (currentMod) {
                            ModuleType.RUNNING -> onSaveRunning(durationMinutes, distanceKm, dateString)
                            ModuleType.STUDY -> onSaveStudy(durationMinutes, studySubject, dateString)
                            ModuleType.GYM -> onSaveGym(durationMinutes, dateString)
                            ModuleType.PRAYER -> onSavePrayer(prayerCompleted, dateString)
                            ModuleType.BUSINESS -> onSaveBusiness(durationMinutes, dateString)
                            ModuleType.FOOTBALL -> onSaveFootball(durationMinutes, dateString)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_quick_input_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = modColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE SESSION (+10 XP)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
