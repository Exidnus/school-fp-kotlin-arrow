package com.school.model

import arrow.Kind
import java.time.Instant

data class User(val id: Int, val name: String, val email: String, val password: String)

data class Lesson(val id: LessonId,
                  val modelVersion: Int,
                  val teacherId: ParticipantId,
                  val students: Map<Int, Participant>,
                  val subject: String,
                  val description: String,
                  val beginTime: Instant,
                  val endTime: Instant)


data class Participant(val id: ParticipantId,
                       val name: String,
                       val handRaised: Boolean)

data class LoadedLesson(val id: LessonId,
                        val teacherId: ParticipantId,
                        val students: List<LoadedParticipant>,
                        val subject: String,
                        val description: String,
                        val beginTime: Instant,
                        val endTime: Instant)

fun LoadedLesson.toRuntime(): Lesson =
        Lesson(
                id,
                modelVersion = 1,
                teacherId = teacherId,
                students = students.map { Pair(it.id, it.toRuntime()) }.toMap(),
                subject = subject,
                description = description,
                beginTime = beginTime,
                endTime = endTime
        )

data class LoadedParticipant(val id: ParticipantId,
                             val name: String)

fun LoadedParticipant.toRuntime(): Participant =
        Participant(
                id,
                name,
                handRaised = false
        )

fun Participant.raiseHand(): Participant = copy(handRaised = true)

sealed class Notification {
    data class StartPaint(val line: String) : Notification()
    data class FinishPaint(val line: String) : Notification()
}

typealias LessonId = Int
typealias ParticipantId = Int
typealias PageId = Int
typealias NotebookId = Int