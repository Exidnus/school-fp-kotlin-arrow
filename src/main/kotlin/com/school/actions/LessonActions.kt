package com.school.actions

import arrow.Kind
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import arrow.typeclasses.Applicative
import arrow.typeclasses.Monad
import com.school.*
import com.school.model.*

class LessonJoin<F>(private val saveNewcomerStudent: (ParticipantId) -> Kind<F, Unit>,
                    monad: Monad<F>) : Monad<F> by monad {
    fun run(lesson: Lesson, participantId: Int, name: String): Kind<F, Result<EmptyLessonState>> =
            when (val findOpt = lesson.findParticipantOpt(participantId)) {
                is Some -> {
                    if (findOpt.t.state == Participant.State.PRESENT) {
                        Result.pure(LessonState.emptyState(lesson)).just()
                    } else {
                        Result.pure(
                                LessonState.emptyState(lesson.copy(participants = lesson.participants + Pair(participantId, findOpt.t.join()))),
                                listOf(Notification.ParticipantJoined(lesson.id, findOpt.t.toInfo()))
                        ).just()
                    }
                }
                is None -> {
                    val newStudent = Participant.newStudent(participantId, name)
                    saveNewcomerStudent(participantId).map {
                        Result.pure(
                                LessonState.emptyState(lesson.copy(participants = lesson.participants + Pair(participantId, newStudent))),
                                listOf(Notification.ParticipantJoined(lesson.id, newStudent.toInfo()))
                        )
                    }
                }
            }
}

class LessonRaiseHand<F>(applicative: Applicative<F>) : Applicative<F> by applicative {
    fun run(lesson: Lesson, participantId: Int): Kind<F, Result<EmptyLessonState>> =
            lesson.findParticipantRes(participantId)
                    .map { it.raiseHand() }
                    .map { LessonState.emptyState(lesson.copy(participants = lesson.participants + Pair(participantId, it))) }
                    .appendNotifications(listOf(Notification.HandPositionChanged(lesson.id, participantId, handRaised = true)))
                    .just()
}

fun Lesson.findParticipantRes(participantId: ParticipantId): Result<Participant> =
        participants[participantId]
                .toResult(ErrorMsg.PARTICIPANT_NOT_FOUND)

fun Lesson.findParticipantOpt(participantId: Int): Option<Participant> =
        participants[participantId]
                .toOption()