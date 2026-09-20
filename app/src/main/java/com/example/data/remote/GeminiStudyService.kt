package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.StudyTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiStudyService {
    private val service = RetrofitClient.geminiService
    private val moshi = RetrofitClient.moshi

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") "" else key
    }

    suspend fun generateSchedulePlan(
        subjects: List<String>,
        daysCount: Int,
        hoursPerDay: Float,
        difficultyLevel: String,
        primaryMethod: String
    ): Result<List<StudyTask>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            // Provide high-quality local adaptive schedule if API key is not yet set
            Log.w("GeminiStudyService", "GEMINI_API_KEY is not configured; using offline smart planner.")
            return@withContext Result.success(
                generateOfflineSmartPlan(subjects, daysCount, hoursPerDay, primaryMethod)
            )
        }

        val subjectListStr = subjects.joinToString(", ")
        val totalMinutesPerDay = (hoursPerDay * 60).toInt()

        val prompt = """
You are an expert AI Academic Coach and Study Schedule Optimizer.
Create a structured $daysCount-day study schedule for a student studying: $subjectListStr.
Available study time: $hoursPerDay hours ($totalMinutesPerDay minutes) per day.
Preparation level: $difficultyLevel.
Preferred study methodology: $primaryMethod.

Return ONLY a valid JSON object matching this schema:
{
  "planTitle": "Study Plan Title",
  "overview": "Brief strategic plan summary",
  "tasks": [
    {
      "dayIndex": 0,
      "dayLabel": "Day 1 - Foundations",
      "subject": "Subject Name",
      "title": "Clear concise topic or task title",
      "description": "Specific action item with learning technique (e.g. solve 10 problems, create flashcards)",
      "durationMinutes": 45,
      "priority": "HIGH",
      "studyMethod": "$primaryMethod"
    }
  ]
}
Requirements:
1. dayIndex must go from 0 up to ${daysCount - 1}.
2. Ensure realistic study sessions (25 to 60 minutes each). Total minutes per day should roughly match $totalMinutesPerDay minutes.
3. Distribute the subjects evenly across the days.
4. Include active recall, practice problems, or review milestones for mastery.
5. priority must be one of: "HIGH", "MEDIUM", "LOW".
6. Do NOT wrap output in markdown tags other than standard json or plain text.
""".trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.5f,
                responseMimeType = "application/json"
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(text = "You are an elite study strategist who generates rigorous, realistic, science-backed study schedules in pure JSON.")
                )
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response from Gemini API")

            val cleanedJson = cleanJsonString(responseText)
            val adapter = moshi.adapter(GeneratedPlanResponse::class.java)
            val parsed = adapter.fromJson(cleanedJson)

            val taskItems = parsed?.tasks?.mapIndexed { index, item ->
                StudyTask(
                    dayIndex = item.dayIndex.coerceAtLeast(0),
                    dayLabel = if (item.dayLabel.isNotBlank()) item.dayLabel else "Day ${item.dayIndex + 1}",
                    subject = item.subject.ifBlank { subjects.getOrElse(index % subjects.size) { "General" } },
                    title = item.title.ifBlank { "Study Session ${index + 1}" },
                    description = item.description.ifBlank { "Focus session on core concepts and active recall." },
                    durationMinutes = item.durationMinutes.coerceIn(15, 120),
                    priority = if (item.priority in listOf("HIGH", "MEDIUM", "LOW")) item.priority else "MEDIUM",
                    isCompleted = false,
                    studyMethod = item.studyMethod.ifBlank { primaryMethod }
                )
            } ?: emptyList()

            if (taskItems.isNotEmpty()) {
                Result.success(taskItems)
            } else {
                Result.success(generateOfflineSmartPlan(subjects, daysCount, hoursPerDay, primaryMethod))
            }
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "API generation failed, falling back to local smart plan", e)
            Result.success(generateOfflineSmartPlan(subjects, daysCount, hoursPerDay, primaryMethod))
        }
    }

    suspend fun askTutor(topicOrQuestion: String, subject: String = ""): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(
                "💡 **Study AI Tip**: To enable real-time AI tutoring with Gemini, please add your `GEMINI_API_KEY` in the AI Studio Secrets panel.\n\n" +
                "**Quick Explanation for '$topicOrQuestion'**:\n" +
                "1. **Core Concept**: Break the topic into foundational axioms before memorizing details.\n" +
                "2. **Feynman Technique**: Explain it out loud to yourself as if teaching a 10-year-old. Wherever you hesitate is where your understanding has a gap.\n" +
                "3. **Active Recall**: Close your notes and write down key formulas or definitions from memory."
            )
        }

        val subjectContext = if (subject.isNotBlank()) "in the context of $subject" else ""
        val prompt = """
You are a brilliant, supportive, and articulate AI Study Tutor.
A student needs help with this topic or question $subjectContext:
"$topicOrQuestion"

Please provide:
1. **Plain-English Explanation**: Clear intuition and analogy (avoid overly academic jargon).
2. **Key Concepts / Formulas**: Essential points to remember.
3. **Real-world Example or Worked Problem**: Walk through how it applies.
4. **Active Recall Check**: 1 quick self-test question for the student to verify they understood.

Keep the response structured, formatted with markdown bold headings and bullet points.
""".trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.6f)
        )

        try {
            val response = service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I couldn't generate an answer right now. Please try again."
            Result.success(text)
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "Tutor request error", e)
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(topic: String, count: Int = 4): Result<List<QuizQuestionItem>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getFallbackQuiz(topic))
        }

        val prompt = """
Create a high-yield study practice quiz on the topic: "$topic".
Generate $count multiple choice questions.

Return ONLY a valid JSON object matching this schema:
{
  "topic": "$topic",
  "questions": [
    {
      "question": "Clear question text?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "Why this answer is correct and key takeaway."
    }
  ]
}
Requirements:
1. Exactly 4 options per question.
2. correctIndex must be an integer from 0 to 3.
3. Keep questions testing deep conceptual understanding.
""".trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.4f,
                responseMimeType = "application/json"
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response")
            val cleaned = cleanJsonString(text)
            val adapter = moshi.adapter(GeneratedQuizResponse::class.java)
            val parsed = adapter.fromJson(cleaned)
            val questions = parsed?.questions ?: getFallbackQuiz(topic)
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "Quiz generation failed", e)
            Result.success(getFallbackQuiz(topic))
        }
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.substringBeforeLast("```")
        }
        return clean.trim()
    }

    private fun generateOfflineSmartPlan(
        subjects: List<String>,
        daysCount: Int,
        hoursPerDay: Float,
        studyMethod: String
    ): List<StudyTask> {
        val validSubjects = if (subjects.isEmpty()) listOf("Core Subject") else subjects
        val tasks = mutableListOf<StudyTask>()
        val tasksPerDay = (hoursPerDay * 2).toInt().coerceIn(1, 4)
        val minutesPerTask = ((hoursPerDay * 60) / tasksPerDay).toInt().coerceIn(25, 60)

        val taskThemes = listOf(
            "Foundations & Core Theory Review" to "Active recall of chapter definitions, key axioms, and theorems.",
            "Deep Problem Solving Session" to "Work through 8-12 textbook practice problems without looking at solutions.",
            "Active Recall & Flashcard Drill" to "Spaced repetition drill on high-yield formulas and concepts.",
            "Error Analysis & Weak Spot Refinement" to "Review previous quiz mistakes and rework difficult problems.",
            "Synthesis & Concept Mapping" to "Draw a mind-map linking main concepts and test retention with Feynman technique.",
            "Timed Mock Practice Quiz" to "Simulate exam conditions with a 30-minute timed self-assessment.",
            "Comprehensive Revision & Mastery" to "Final high-level consolidation and summary sheet creation."
        )

        for (day in 0 until daysCount) {
            val dayLabel = "Day ${day + 1}"
            for (t in 0 until tasksPerDay) {
                val subject = validSubjects[(day * tasksPerDay + t) % validSubjects.size]
                val (titlePrefix, desc) = taskThemes[(day * tasksPerDay + t) % taskThemes.size]
                val priority = when {
                    t == 0 -> "HIGH"
                    t == tasksPerDay - 1 -> "LOW"
                    else -> "MEDIUM"
                }

                tasks.add(
                    StudyTask(
                        dayIndex = day,
                        dayLabel = dayLabel,
                        subject = subject,
                        title = "$subject: $titlePrefix",
                        description = desc,
                        durationMinutes = minutesPerTask,
                        priority = priority,
                        isCompleted = false,
                        studyMethod = studyMethod
                    )
                )
            }
        }
        return tasks
    }

    private fun getFallbackQuiz(topic: String): List<QuizQuestionItem> {
        return listOf(
            QuizQuestionItem(
                question = "When studying '$topic', what is the most scientifically proven method for long-term retention?",
                options = listOf(
                    "Active Recall & Spaced Repetition",
                    "Rereading the textbook multiple times",
                    "Highlighting almost every line",
                    "Cramming 8 hours the night before"
                ),
                correctIndex = 0,
                explanation = "Testing yourself actively forces cognitive retrieval, creating stronger neural pathways than passive rereading."
            ),
            QuizQuestionItem(
                question = "What is the primary benefit of the Feynman Technique?",
                options = listOf(
                    "It minimizes the need to solve problems",
                    "It exposes gaps in understanding by explaining concepts simply",
                    "It allows you to study while multitasking",
                    "It speeds up typing notes"
                ),
                correctIndex = 1,
                explanation = "If you cannot explain a concept simply without jargon, you have identified an exact gap in your foundational knowledge."
            ),
            QuizQuestionItem(
                question = "In the Pomodoro technique for deep work, what is the standard recommended study interval?",
                options = listOf(
                    "10 minutes study, 10 minutes break",
                    "25 minutes focused study, 5 minutes break",
                    "120 minutes continuous study, no break",
                    "45 minutes social media, 15 minutes study"
                ),
                correctIndex = 1,
                explanation = "25 minutes of uninterrupted focus followed by a 5-minute cognitive reset maintains peak attention and prevents burnout."
            )
        )
    }
}
