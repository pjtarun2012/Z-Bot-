package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "error") val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: Int? = null,
    @Json(name = "status") val status: String? = null
)

// Data classes for parsed JSON responses from Gemini
@JsonClass(generateAdapter = true)
data class GeneratedPlanResponse(
    @Json(name = "planTitle") val planTitle: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "tasks") val tasks: List<GeneratedTaskItem>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedTaskItem(
    @Json(name = "dayIndex") val dayIndex: Int = 0,
    @Json(name = "dayLabel") val dayLabel: String = "Day 1",
    @Json(name = "subject") val subject: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "durationMinutes") val durationMinutes: Int = 30,
    @Json(name = "priority") val priority: String = "MEDIUM",
    @Json(name = "studyMethod") val studyMethod: String = "Active Recall"
)

@JsonClass(generateAdapter = true)
data class GeneratedQuizResponse(
    @Json(name = "topic") val topic: String? = null,
    @Json(name = "questions") val questions: List<QuizQuestionItem>? = null
)

@JsonClass(generateAdapter = true)
data class QuizQuestionItem(
    @Json(name = "question") val question: String = "",
    @Json(name = "options") val options: List<String> = emptyList(),
    @Json(name = "correctIndex") val correctIndex: Int = 0,
    @Json(name = "explanation") val explanation: String = ""
)
