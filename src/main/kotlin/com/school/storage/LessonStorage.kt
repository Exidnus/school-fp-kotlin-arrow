package com.school.storage

import arrow.Kind
import com.school.service.LessonContainer

interface LessonStorage<F> {
    fun addLesson(newLessonInfo: LessonContainer.LessonInfo): Kind<F, Int>
}