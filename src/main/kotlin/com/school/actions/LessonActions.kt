package com.school.actions

import arrow.Kind
import arrow.core.*
import arrow.fx.typeclasses.MonadDefer
import arrow.typeclasses.Applicative
import arrow.typeclasses.Monad
import com.school.*
import com.school.model.*
import com.school.service.LessonContainer
import com.school.storage.tx

class LessonJoin<F>(private val saveNewcomerStudent: (ParticipantId) -> Kind<F, Unit>,
                    private val monad: MonadDefer<F>) : Monad<F> by monad {
    fun run(lesson: Lesson, participantId: Int, name: String): Kind<F, Result<EmptyLessonState>> =
            when (val findOpt = lesson.findParticipantOpt(participantId)) {
                is Some ->
                    processParticipantComesAgain(lesson, findOpt.t)
                is None ->
                    processNewcomerStudent(lesson, participantId, name)
            }

    private fun processParticipantComesAgain(lesson: Lesson, participant: Participant): Kind<F, Result<EmptyLessonState>> =
            if (participant.state == Participant.State.PRESENT) {
                Result.pure(LessonState.emptyState(lesson)).just()
            } else {
                Result.pure(
                        LessonState.emptyState(lesson.copy(participants = lesson.participants + Pair(participant.id, participant.join()))),
                        listOf(Notification.ParticipantJoined(lesson.id, participant.toInfo()))
                ).just()
            }

    private fun processNewcomerStudent(lesson: Lesson, participantId: Int, name: String): Kind<F, Result<EmptyLessonState>> {
        val newStudent = Participant.newStudent(participantId, name)
        return tx(monad)
                .use {
                    saveNewcomerStudent(participantId)
                }
                .map {
                    Result.pure(
                            LessonState.emptyState(lesson.copy(participants = lesson.participants + Pair(participantId, newStudent))),
                            listOf(Notification.ParticipantJoined(lesson.id, newStudent.toInfo()))
                    )
                }
    }
}

class LessonChange<F> {
    fun run(lesson: Lesson, updateInfo: LessonContainer.LessonInfo): Kind<F, Result<EmptyLessonState>> = TODO()
}

class LessonRaiseHand<F>(applicative: Applicative<F>) : Applicative<F> by applicative {
    fun run(lesson: Lesson, participantId: Int): Kind<F, Result<EmptyLessonState>> =
            lesson.findParticipantRes(participantId)
                    .map { it.raiseHand() }
                    .map { LessonState.emptyState(lesson.copy(participants = lesson.participants + Pair(participantId, it))) }
                    .appendNotifications(listOf(Notification.HandPositionChanged(lesson.id, participantId, handRaised = true)))
                    .just()
}

class LessonUpdateInactivityTime<F>(private val time: Time<F>,
                                    monad: Monad<F>) : Monad<F> by monad {
    fun run(lesson: Lesson): Kind<F, Result<EmptyLessonState>> {
        val newLesson = if (lesson.participants.values.all { it.state == Participant.State.NOT_PRESENT }) {
            time.now().map { now ->
                lesson.copy(lastInactiveTime = lesson.lastInactiveTime.getOrElse { now }.some())
            }
        } else {
            lesson.just()
        }
        return newLesson.map { Result.pure(LessonState.emptyState(it)) }
    }
}

fun Lesson.findParticipantRes(participantId: ParticipantId): Result<Participant> =
        participants[participantId]
                .toResult(ErrorMsg.PARTICIPANT_NOT_FOUND)

fun Lesson.findParticipantOpt(participantId: Int): Option<Participant> =
        participants[participantId]
                .toOption()