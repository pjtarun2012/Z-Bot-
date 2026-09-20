package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel
) {
    val tasks by viewModel.tasks.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val completionPercent = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

    val totalSessionMinutes = remember(sessions) { sessions.sumOf { it.durationMinutes } }
    val totalTaskMinutesCompleted = remember(tasks) {
        tasks.filter { it.isCompleted }.sumOf { it.durationMinutes }
    }
    val combinedMinutes = totalSessionMinutes + totalTaskMinutesCompleted
    val totalHours = combinedMinutes / 60
    val remainingMins = combinedMinutes % 60

    // Subject breakdown
    val subjectStats = remember(tasks, sessions) {
        val map = mutableMapOf<String, Int>()
        tasks.forEach {
            if (it.isCompleted) {
                map[it.subject] = (map[it.subject] ?: 0) + it.durationMinutes
            }
        }
        sessions.forEach {
            map[it.subject] = (map[it.subject] ?: 0) + it.durationMinutes
        }
        map.toList().sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Study Analytics & Progress",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Stats Grid (2x2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Study Time",
                        value = if (totalHours > 0) "${totalHours}h ${remainingMins}m" else "${remainingMins}m",
                        subtext = "${sessions.size} focus sessions",
                        icon = Icons.Default.HourglassFull,
                        containerColor = IndigoContainer,
                        iconColor = IndigoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Tasks Completed",
                        value = "$completedTasks / $totalTasks",
                        subtext = "$completionPercent% completion rate",
                        icon = Icons.Default.TaskAlt,
                        containerColor = EmeraldContainer,
                        iconColor = EmeraldTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Active Study Days",
                        value = "${if (tasks.isNotEmpty()) tasks.map { it.dayIndex }.distinct().size else 0} Days",
                        subtext = "Structured schedule",
                        icon = Icons.Default.CalendarToday,
                        containerColor = SkyContainer,
                        iconColor = SkySecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Retention Quality",
                        value = if (completionPercent >= 70) "High" else if (completionPercent >= 30) "Moderate" else "Building",
                        subtext = "Active recall & review",
                        icon = Icons.Default.AutoAwesome,
                        containerColor = AmberContainer,
                        iconColor = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // AI Study Insights Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cognitive Optimization Tip",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (completionPercent >= 80) {
                                "🔥 Superb execution! To cement this material in long-term memory, do a quick 10-minute active recall self-test 48 hours after your main study block."
                            } else if (completedTasks > 0) {
                                "🧠 Great momentum! Remember: sleep plays an active role in memory consolidation. Avoid late-night cramming and maintain your daily Pomodoro blocks."
                            } else {
                                "✨ Start small! Even a single 25-minute Pomodoro focus block on your highest-priority subject overcomes procrastination and builds study inertia."
                            },
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Subject Breakdown List
            item {
                Text(
                    text = "Subject Time Investment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            if (subjectStats.isEmpty()) {
                item {
                    Text(
                        text = "Complete scheduled tasks or run focus timer sessions to populate subject statistics.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                val maxMinutes = subjectStats.maxOfOrNull { it.second } ?: 1
                items(subjectStats) { (subject, mins) ->
                    val color = remember(subject) { getSubjectBadgeColor(subject) }
                    val fraction = mins.toFloat() / maxMinutes

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subject,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "${mins / 60}h ${mins % 60}m",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
