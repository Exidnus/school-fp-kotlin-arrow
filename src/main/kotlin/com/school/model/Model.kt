package com.school.model

import java.time.Instant

data class User(val id: Int, val name: String, val email: String, val password: String)

data class Lesson(val id: LessonId,
                  val modelVersion: Int,
                  val participants: Map<Int, Participant>,
                  val subject: String,
                  val description: String,
                  val beginTime: Instant,
                  val endTime: Instant)

data class LessonState<out A>(val lesson: Lesson, val a: A) {
    companion object {
        fun emptyState(lesson: Lesson): EmptyLessonState =
                LessonState(lesson, Unit)
    }
}

typealias EmptyLessonState = LessonState<Unit>


data class Participant(val id: ParticipantId,
                       val name: String,
                       val type: Type,
                       val state: State,
                       val handRaised: Boolean) {
    companion object {
        fun newStudent(id: ParticipantId,
                       name: String): Participant =
                Participant(id,
                        name,
                        Type.STUDENT,
                        State.PRESENT,
                        handRaised = false)
    }

    enum class Type {
        TEACHER,
        STUDENT
    }

    enum class State {
        NOT_PRESENT,
        LOST,
        PRESENT
    }
}

data class LoadedLesson(val id: LessonId,
                        val teacher: LoadedParticipant,
                        val students: List<LoadedParticipant>,
                        val subject: String,
                        val description: String,
                        val beginTime: Instant,
                        val endTime: Instant)

fun LoadedLesson.toRuntime(): Lesson =
        Lesson(
                id,
                modelVersion = 1,
                participants = students.map { Pair(it.id, it.toRuntime(isTeacher = false)) }.toMap() +
                        mapOf(Pair(teacher.id, teacher.toRuntime(isTeacher = true))),
                subject = subject,
                description = description,
                beginTime = beginTime,
                endTime = endTime
        )

data class LoadedParticipant(val id: ParticipantId,
                             val name: String)

fun LoadedParticipant.toRuntime(isTeacher: Boolean): Participant =
        Participant(
                id,
                name,
                type = if (isTeacher) Participant.Type.TEACHER else Participant.Type.STUDENT,
                state = Participant.State.NOT_PRESENT,
                handRaised = false
        )

fun Participant.raiseHand(): Participant = copy(handRaised = true)

fun Participant.lowerHand(): Participant = copy(handRaised = false)

fun Participant.join(): Participant = copy(state = Participant.State.PRESENT)

data class ParticipantJoinInfo(val participantId: ParticipantId,
                               val name: String,
                               val type: Participant.Type)

fun Participant.toInfo(): ParticipantJoinInfo =
        ParticipantJoinInfo(id,
                name,
                type)

sealed class Notification {
    data class ParticipantJoined(val lessonId: LessonId,
                                 val participant: ParticipantJoinInfo) : Notification()

    data class HandPositionChanged(val lessonId: LessonId,
                                   val participantId: ParticipantId,
                                   val handRaised: Boolean) : Notification()

    data class StartPaint(val line: String) : Notification()
    data class FinishPaint(val line: String) : Notification()
}

typealias UserId = Int
typealias LessonId = Int
typealias ParticipantId = Int
typealias PageId = Int
typealias NotebookId = Int