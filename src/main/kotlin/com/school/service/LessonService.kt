package com.school.service

import arrow.Kind
import com.school.Result
import com.school.model.Lesson
import com.school.model.LessonState

interface LessonService<F> {
    fun <A> run(action: (Lesson) -> Kind<F, Result<LessonState<A>>>): Kind<F, Result<A>>
//    fun lesson(): Kind<F, Result<Lesson>>
//
//    fun joinLesson(participantId: Int, name: String): Kind<F, Result<Unit>>
//
//    fun raiseHand(participantId: Int): Kind<F, Result<Unit>>
}