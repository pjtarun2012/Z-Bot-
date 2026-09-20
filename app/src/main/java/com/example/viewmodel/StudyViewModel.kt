package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.StudyRepository
import com.example.data.local.StudySession
import com.example.data.local.StudyTask
import com.example.data.remote.GeminiStudyService
import com.example.data.remote.QuizQuestionItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TutorMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyRepository
    private val geminiService = GeminiStudyService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(db.studyDao())
    }

    // Tasks flow from Room
    val tasks: StateFlow<List<StudyTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sessions flow from Room
    val sessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected Day filter (0 for Day 1, null for All Days)
    private val _selectedDayIndex = MutableStateFlow<Int?>(null)
    val selectedDayIndex: StateFlow<Int?> = _selectedDayIndex.asStateFlow()

    // Plan Generator State
    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _planStatusMessage = MutableStateFlow<String?>(null)
    val planStatusMessage: StateFlow<String?> = _planStatusMessage.asStateFlow()

    // Tutor Chat State
    private val _tutorMessages = MutableStateFlow<List<TutorMessage>>(
        listOf(
            TutorMessage(
                isUser = false,
                text = "Hello! I am your AI Study Coach & Tutor 🎓.\n\nAsk me to explain any difficult concept, break down complex formulas, generate flashcards, or create customized quizzes for your exams!"
            )
        )
    )
    val tutorMessages: StateFlow<List<TutorMessage>> = _tutorMessages.asStateFlow()

    private val _isTutorThinking = MutableStateFlow(false)
    val isTutorThinking: StateFlow<Boolean> = _isTutorThinking.asStateFlow()

    // Quiz State
    private val _quizQuestions = MutableStateFlow<List<QuizQuestionItem>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestionItem>> = _quizQuestions.asStateFlow()

    private val _quizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quizAnswers: StateFlow<Map<Int, Int>> = _quizAnswers.asStateFlow()

    private val _isQuizSubmitted = MutableStateFlow(false)
    val isQuizSubmitted: StateFlow<Boolean> = _isQuizSubmitted.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    // Focus Timer State
    private val _timerDurationSeconds = MutableStateFlow(25 * 60)
    val timerDurationSeconds: StateFlow<Int> = _timerDurationSeconds.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow(25 * 60)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSubject = MutableStateFlow("General Study")
    val timerSubject: StateFlow<String> = _timerSubject.asStateFlow()

    private var timerJob: Job? = null

    fun selectDayIndex(day: Int?) {
        _selectedDayIndex.value = day
    }

    // Generate AI Study Schedule
    fun generateSchedule(
        subjects: List<String>,
        daysCount: Int,
        hoursPerDay: Float,
        difficulty: String,
        method: String,
        replaceExisting: Boolean = true
    ) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            _planStatusMessage.value = "AI is tailoring your personalized study schedule..."
            try {
                val result = geminiService.generateSchedulePlan(
                    subjects = subjects,
                    daysCount = daysCount,
                    hoursPerDay = hoursPerDay,
                    difficultyLevel = difficulty,
                    primaryMethod = method
                )
                result.onSuccess { newTasks ->
                    if (replaceExisting) {
                        repository.clearAllTasks()
                    }
                    repository.insertTasks(newTasks)
                    _planStatusMessage.value = "Successfully generated ${newTasks.size} study tasks across $daysCount days!"
                    _selectedDayIndex.value = 0 // focus on Day 1
                }.onFailure { err ->
                    _planStatusMessage.value = "Plan generation error: ${err.message}"
                }
            } catch (e: Exception) {
                _planStatusMessage.value = "Error: ${e.message}"
            } finally {
                _isGeneratingPlan.value = false
            }
        }
    }

    fun toggleTaskCompletion(taskId: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompletion(taskId, completed)
        }
    }

    fun addTask(task: StudyTask) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
        }
    }

    fun clearPlanStatusMessage() {
        _planStatusMessage.value = null
    }

    // Tutor
    fun askTutor(question: String, subject: String = "") {
        if (question.isBlank()) return
        val userMsg = TutorMessage(isUser = true, text = question)
        _tutorMessages.value = _tutorMessages.value + userMsg
        _isTutorThinking.value = true

        viewModelScope.launch {
            try {
                val result = geminiService.askTutor(question, subject)
                val replyText = result.getOrElse {
                    "Sorry, I encountered an issue connecting to Gemini: ${it.message}"
                }
                _tutorMessages.value = _tutorMessages.value + TutorMessage(
                    isUser = false,
                    text = replyText
                )
            } finally {
                _isTutorThinking.value = false
            }
        }
    }

    fun clearTutorChat() {
        _tutorMessages.value = listOf(
            TutorMessage(
                isUser = false,
                text = "Chat cleared. What study topic would you like help with?"
            )
        )
    }

    // Quiz
    fun generateQuiz(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            _quizAnswers.value = emptyMap()
            _isQuizSubmitted.value = false
            try {
                val result = geminiService.generateQuiz(topic)
                _quizQuestions.value = result.getOrElse { emptyList() }
            } finally {
                _isGeneratingQuiz.value = false
            }
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        if (_isQuizSubmitted.value) return
        _quizAnswers.value = _quizAnswers.value + (questionIndex to optionIndex)
    }

    fun submitQuiz() {
        _isQuizSubmitted.value = true
    }

    fun resetQuiz() {
        _quizQuestions.value = emptyList()
        _quizAnswers.value = emptyMap()
        _isQuizSubmitted.value = false
    }

    // Focus Timer
    fun setTimerPreset(minutes: Int, subject: String = _timerSubject.value) {
        pauseTimer()
        _timerDurationSeconds.value = minutes * 60
        _timerSecondsLeft.value = minutes * 60
        _timerSubject.value = subject
    }

    fun setTimerSubject(subject: String) {
        _timerSubject.value = subject
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timerSecondsLeft.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _timerSecondsLeft.value -= 1
            }
            if (_timerSecondsLeft.value <= 0) {
                _isTimerRunning.value = false
                val completedMinutes = _timerDurationSeconds.value / 60
                if (completedMinutes > 0) {
                    repository.insertSession(
                        StudySession(
                            subject = _timerSubject.value,
                            durationMinutes = completedMinutes,
                            notes = "Pomodoro focus block completed"
                        )
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        pauseTimer()
        _timerSecondsLeft.value = _timerDurationSeconds.value
    }

    fun finishAndSaveTimerEarly() {
        val elapsedSeconds = _timerDurationSeconds.value - _timerSecondsLeft.value
        val elapsedMinutes = elapsedSeconds / 60
        if (elapsedMinutes >= 1) {
            viewModelScope.launch {
                repository.insertSession(
                    StudySession(
                        subject = _timerSubject.value,
                        durationMinutes = elapsedMinutes,
                        notes = "Completed focus session"
                    )
                )
            }
        }
        resetTimer()
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSessionById(sessionId)
        }
    }
}
