package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dayIndex: Int = 0, // 0 = Day 1 / Today, 1 = Day 2, etc.
    val dayLabel: String = "Day 1", // e.g. "Monday", "Day 1 - Foundations"
    val subject: String,
    val title: String,
    val description: String,
    val durationMinutes: Int = 30,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false,
    val studyMethod: String = "Active Recall", // Pomodoro, Active Recall, Practice Questions, Feynman Technique, Summary
    val createdAt: Long = System.currentTimeMillis()
)
