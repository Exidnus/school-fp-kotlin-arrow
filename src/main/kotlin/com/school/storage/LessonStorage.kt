package com.school.storage

import arrow.Kind
import com.school.service.LessonContainer

interface LessonStorage<F> {
    fun addLesson(newLessonInfo: LessonContainer.NewLessonInfo): Kind<F, Int>
}