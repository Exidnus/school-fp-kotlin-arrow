package com.school.service

import arrow.Kind
import com.school.Result
import com.school.model.Lesson

interface LessonService<F> {
    /**
     * Текущее состояние урока.
     */
    fun lesson(): Kind<F, Result<Lesson>>

    fun joinLesson(participantId: Int, name: String): Kind<F, Result<Unit>>

    fun raiseHand(participantId: Int): Kind<F, Result<Unit>>
}