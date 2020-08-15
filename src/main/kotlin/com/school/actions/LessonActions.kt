package com.school.actions

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import com.school.*
import com.school.model.*

fun Lesson.join(participantId: Int, name: String): Result<EmptyLessonState> =
        when (val findOpt = findParticipantOpt(participantId)) {
            is Some -> {
                if (findOpt.t.state == Participant.State.PRESENT) {
                    Result.pure(LessonState.emptyState(this))
                } else {
                    Result.pure(
                            LessonState.emptyState(this.copy(participants = this.participants + Pair(participantId, findOpt.t.join()))),
                            listOf(Notification.ParticipantJoined(this.id, findOpt.t.toInfo()))
                    )
                }
            }
            is None -> {
                val newStudent = Participant.newStudent(participantId, name)
                Result.pure(
                        LessonState.emptyState(this.copy(participants = this.participants + Pair(participantId, newStudent))),
                        listOf(Notification.ParticipantJoined(this.id, newStudent.toInfo()))
                )
            }
        }


fun Lesson.raiseHand(participantId: Int): Result<EmptyLessonState> =
        this.findParticipantRes(participantId)
                .map { it.raiseHand() }
                .map { LessonState.emptyState(this.copy(participants = this.participants + Pair(participantId, it))) }
                .appendNotifications(listOf(Notification.HandPositionChanged(this.id, participantId, handRaised = true)))

fun Lesson.findParticipantRes(participantId: ParticipantId): Result<Participant> =
        participants[participantId]
                .toResult(ErrorMsg.PARTICIPANT_NOT_FOUND)

fun Lesson.findParticipantOpt(participantId: Int): Option<Participant> =
        participants[participantId]
                .toOption()