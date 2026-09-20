package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.StudyViewModel

enum class StudyScreenTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SCHEDULE("Schedule", Icons.AutoMirrored.Filled.EventNote, "tab_schedule"),
    AI_PLANNER("AI Planner", Icons.Default.AutoAwesome, "tab_ai_planner"),
    AI_TUTOR("AI Tutor", Icons.Default.Psychology, "tab_ai_tutor"),
    TIMER("Focus Timer", Icons.Default.Timer, "tab_timer"),
    ANALYTICS("Progress", Icons.Default.Insights, "tab_analytics")
}

@Composable
fun MainScreen(viewModel: StudyViewModel) {
    var currentTab by remember { mutableStateOf(StudyScreenTab.SCHEDULE) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 6.dp
            ) {
                StudyScreenTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                StudyScreenTab.SCHEDULE -> {
                    ScheduleScreen(
                        viewModel = viewModel,
                        onNavigateToAiPlanner = {
                            currentTab = StudyScreenTab.AI_PLANNER
                        },
                        onStartTimerForTask = { subject, minutes ->
                            viewModel.setTimerPreset(minutes, subject)
                            currentTab = StudyScreenTab.TIMER
                            viewModel.startTimer()
                        }
                    )
                }
                StudyScreenTab.AI_PLANNER -> {
                    AiPlannerScreen(
                        viewModel = viewModel,
                        onPlanGenerated = {
                            currentTab = StudyScreenTab.SCHEDULE
                        }
                    )
                }
                StudyScreenTab.AI_TUTOR -> {
                    AiTutorScreen(viewModel = viewModel)
                }
                StudyScreenTab.TIMER -> {
                    TimerScreen(viewModel = viewModel)
                }
                StudyScreenTab.ANALYTICS -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
