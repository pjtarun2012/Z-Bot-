package com.example.data.local

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {
    val allTasks: Flow<List<StudyTask>> = studyDao.getAllTasks()
    val allSessions: Flow<List<StudySession>> = studyDao.getAllSessions()

    fun getTasksByDay(dayIndex: Int): Flow<List<StudyTask>> = studyDao.getTasksByDay(dayIndex)

    suspend fun insertTask(task: StudyTask) = studyDao.insertTask(task)

    suspend fun insertTasks(tasks: List<StudyTask>) = studyDao.insertTasks(tasks)

    suspend fun updateTask(task: StudyTask) = studyDao.updateTask(task)

    suspend fun deleteTask(task: StudyTask) = studyDao.deleteTask(task)

    suspend fun deleteTaskById(taskId: Int) = studyDao.deleteTaskById(taskId)

    suspend fun setTaskCompletion(taskId: Int, isCompleted: Boolean) =
        studyDao.setTaskCompletion(taskId, isCompleted)

    suspend fun clearAllTasks() = studyDao.clearAllTasks()

    suspend fun insertSession(session: StudySession) = studyDao.insertSession(session)

    suspend fun deleteSessionById(sessionId: Int) = studyDao.deleteSessionById(sessionId)
}
