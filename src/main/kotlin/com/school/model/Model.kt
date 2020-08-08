package com.school.model

import arrow.Kind
import java.time.Instant

data class User(val id: Int, val name: String, val email: String, val password: String)

data class Lesson(val id: Int,
                  val teacherId: Int,
                  val students: Map<Int, Participant>,
                  val subject: String,
                  val description: String,
                  val beginTime: Instant,
                  val endTime: Instant)

data class Participant(val id: Int,
                       val name: String,
                       val handRaised: Boolean)

fun Participant.raiseHand(): Participant = copy(handRaised = true)

sealed class Notification {
    data class StartPaint(val line: String) : Notification()
    data class FinishPaint(val line: String) : Notification()
}

typealias PageId = Int
typealias NotebookId = Int