package com.school.service

import arrow.Kind
import arrow.fx.typeclasses.Duration
import com.school.model.Lesson
import com.school.model.LessonId
import com.school.model.UserId
import com.school.Result
import com.school.model.LessonState
import java.time.Instant

interface LessonContainer<F> {
    fun createLesson(newLessonInfo: LessonInfo): Kind<F, Result<Int>>

    data class LessonInfo(val subject: String,
                          val description: String,
                          val beginTime: Instant,
                          val endTime: Instant)

    fun changeLesson(lessonId: LessonId, lessonInfo: LessonInfo): Kind<F, Result<Unit>>

    fun joinLesson(lessonId: LessonId, userId: UserId): Kind<F, Result<Unit>>

    fun <A> perform(lessonId: LessonId, f: (Lesson) -> Kind<F, Result<LessonState<A>>>): Kind<F, Result<A>>

    fun unloadInactive(inactiveTimeout: Duration): Kind<F, Int>
}