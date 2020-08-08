package com.school.service.impl

import arrow.Kind
import com.school.model.Lesson
import com.school.service.LessonService

class LessonServiceImpl<F> : LessonService<F> {
    override fun lesson(): Kind<F, Lesson> {
        TODO("Not yet implemented")
    }

    override fun joinLesson(participantId: Int): Kind<F, Lesson> {
        TODO("Not yet implemented")
    }

    override fun raiseHand(participantId: Int): Kind<F, Lesson> {
        TODO("Not yet implemented")
    }
}