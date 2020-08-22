package com.school.service.impl

import arrow.Kind
import arrow.fx.typeclasses.Concurrent
import com.school.Result
import com.school.fold
import com.school.map
import com.school.model.Lesson
import com.school.model.LessonState
import com.school.model.LoadedLesson
import com.school.model.toRuntime
import com.school.service.LessonService

fun <F> runLesson(loadedLesson: LoadedLesson,
                  concurrent: Concurrent<F>): Kind<F, LessonService<F>> {
    val lessonInitialState = loadedLesson.toRuntime()
    return concurrent.fx.concurrent {
        val lessonRef = Ref(lessonInitialState).bind()
        object : LessonService<F> {
            override fun <A> run(action: (Lesson) -> Kind<F, Result<LessonState<A>>>): Kind<F, Result<A>> =
                concurrent.fx.concurrent {
                    val lesson = lessonRef.get().bind()
                    val updateResult = action(lesson).bind()
                    val newLesson = updateResult.fold({ lesson }, { it.lesson })
                    lessonRef.set(newLesson).bind()
                    updateResult.map { it.a }
                }
        }
    }
}
