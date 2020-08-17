package com.school.storage

import arrow.Kind
import com.school.model.ParticipantId
import com.school.service.LessonContainer

interface LessonStorage<F> {
    fun addLesson(newLessonInfo: LessonContainer.LessonInfo): Kind<F, Int>

    fun addNewcomer(participantId: ParticipantId): Kind<F, Unit>

    fun updateLesson(updateInfo: LessonContainer.LessonInfo): Kind<F, Boolean>
}