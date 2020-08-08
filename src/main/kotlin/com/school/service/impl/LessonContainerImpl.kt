package com.school.service.impl

import arrow.Kind
import com.school.model.Lesson
import com.school.service.LessonContainer
import com.school.storage.LessonStorage

class LessonContainerImpl<F>(private val lessonStorage: LessonStorage<F>) : LessonContainer<F> {
    override fun createLesson(newLessonInfo: LessonContainer.NewLessonInfo): Kind<F, Int> =
            lessonStorage.addLesson(newLessonInfo)

    override fun perform(f: (Lesson) -> Kind<F, Lesson>): Kind<F, Lesson> {
        TODO("Not yet implemented")
    }
}