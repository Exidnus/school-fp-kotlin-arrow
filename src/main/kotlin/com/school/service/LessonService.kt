package com.school.service

import arrow.Kind
import com.school.model.Lesson

interface LessonService<F> {
    /**
     * Текущее состояние урока.
     */
    fun lesson(): Kind<F, Lesson>

    fun joinLesson(participantId: Int): Kind<F, Unit>

    fun raiseHand(participantId: Int): Kind<F, Unit>
}