package com.school.service

import arrow.Kind
import com.school.model.Lesson
import java.time.Instant

interface LessonContainer<F> {
    fun createLesson(newLessonInfo: NewLessonInfo): Kind<F, Int>

    data class NewLessonInfo(val subject: String,
                             val description: String,
                             val beginTime: Instant,
                             val endTime: Instant)

    fun perform(f: (Lesson) -> Kind<F, Lesson>): Kind<F, Lesson>
}