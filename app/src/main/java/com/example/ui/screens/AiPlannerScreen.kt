package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiPlannerScreen(
    viewModel: StudyViewModel,
    onPlanGenerated: () -> Unit
) {
    val isGenerating by viewModel.isGeneratingPlan.collectAsState()
    val statusMessage by viewModel.planStatusMessage.collectAsState()

    var subjectInput by remember { mutableStateOf("") }
    val subjects = remember {
        mutableStateListOf("Calculus", "Physics", "Data Structures")
    }

    val presetSubjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "History", "Computer Science", "Economics", "Literature")

    var selectedDaysCount by remember { mutableIntStateOf(7) }
    val dayOptions = listOf(3, 5, 7, 14, 30)

    var hoursPerDay by remember { mutableFloatStateOf(2.5f) }

    var selectedDifficulty by remember { mutableStateOf("Standard Exam Prep") }
    val difficultyLevels = listOf("Foundational & Basics", "Standard Exam Prep", "Intensive Cram / Mastery")

    var selectedMethod by remember { mutableStateOf("Active Recall & Spaced Repetition") }
    val methods = listOf(
        "Active Recall & Spaced Repetition",
        "Pomodoro Focus Blocks",
        "Feynman Technique & Practice Problems",
        "High-Yield Mock Exam Sprint"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Study Schedule Maker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Banner
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Intelligent Timetable Synthesizer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gemini AI breaks down subjects into balanced daily recall sessions, problem sets, and milestones.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Section 1: Target Subjects
            item {
                Text(
                    text = "1. Target Subjects",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Input + Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        placeholder = { Text("e.g. Organic Chemistry") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_input_field")
                    )
                    Button(
                        onClick = {
                            val trimmed = subjectInput.trim()
                            if (trimmed.isNotBlank() && !subjects.contains(trimmed)) {
                                subjects.add(trimmed)
                                subjectInput = ""
                            }
                        },
                        modifier = Modifier.testTag("add_subject_button")
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Current Selected Subjects Chips
                if (subjects.isNotEmpty()) {
                    Text(
                        text = "Selected (${subjects.size}):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { subj ->
                            InputChip(
                                selected = true,
                                onClick = { subjects.remove(subj) },
                                label = { Text(subj) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick preset subject suggestions
                Text(
                    text = "Quick suggestions:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(presetSubjects) { preset ->
                        AssistChip(
                            onClick = {
                                if (!subjects.contains(preset)) {
                                    subjects.add(preset)
                                }
                            },
                            label = { Text(preset) },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }

            // Section 2: Study Horizon (Days)
            item {
                Text(
                    text = "2. Schedule Horizon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dayOptions.forEach { days ->
                        FilterChip(
                            selected = selectedDaysCount == days,
                            onClick = { selectedDaysCount = days },
                            label = { Text("$days Days") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 3: Daily Study Hours
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. Daily Study Time",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = String.format("%.1f Hours/Day", hoursPerDay),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = hoursPerDay,
                    onValueChange = { hoursPerDay = it },
                    valueRange = 0.5f..8.0f,
                    steps = 14,
                    modifier = Modifier.testTag("study_hours_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("30 mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("4 hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("8 hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Section 4: Prep Level & Strategy
            item {
                Text(
                    text = "4. Preparation Goal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    difficultyLevels.forEach { level ->
                        FilterChip(
                            selected = selectedDifficulty == level,
                            onClick = { selectedDifficulty = level },
                            label = { Text(level) },
                            leadingIcon = if (selectedDifficulty == level) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text(
                    text = "5. Learning Strategy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = selectedMethod == m,
                            onClick = { selectedMethod = m },
                            label = { Text(m) },
                            leadingIcon = if (selectedMethod == m) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Generation Status / Feedback
            item {
                AnimatedVisibility(visible = statusMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (statusMessage?.contains("Error", ignoreCase = true) == true) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (statusMessage?.contains("Error", ignoreCase = true) == true) {
                                    Icons.Default.ErrorOutline
                                } else {
                                    Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = if (statusMessage?.contains("Error", ignoreCase = true) == true) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (statusMessage?.contains("Error", ignoreCase = true) == true) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Action Button
            item {
                Button(
                    onClick = {
                        if (subjects.isNotEmpty()) {
                            viewModel.generateSchedule(
                                subjects = subjects.toList(),
                                daysCount = selectedDaysCount,
                                hoursPerDay = hoursPerDay,
                                difficulty = selectedDifficulty,
                                method = selectedMethod,
                                replaceExisting = true
                            )
                        }
                    },
                    enabled = !isGenerating && subjects.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_schedule_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI Generating Timetable...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Study Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (statusMessage != null && !isGenerating) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onPlanGenerated,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("View Generated Schedule in Timetable →")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
