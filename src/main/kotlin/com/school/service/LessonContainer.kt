package com.school.service

import arrow.Kind
import com.school.model.Lesson
import com.school.model.LessonId
import com.school.model.UserId
import java.time.Instant

interface LessonContainer<F> {
    fun createLesson(newLessonInfo: NewLessonInfo): Kind<F, Int>

    data class NewLessonInfo(val subject: String,
                             val description: String,
                             val beginTime: Instant,
                             val endTime: Instant)

    fun joinLesson(lessonId: LessonId, userId: UserId): Kind<F, Result<Unit>>

    fun perform(f: (Lesson) -> Kind<F, Lesson>): Kind<F, Result<Unit>>
}