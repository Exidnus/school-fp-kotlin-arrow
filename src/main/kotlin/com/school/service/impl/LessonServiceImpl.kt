package com.school.service.impl

import arrow.Kind
import arrow.core.Tuple2
import arrow.fx.Ref
import arrow.fx.typeclasses.Concurrent
import com.school.Result
import com.school.actions.join
import com.school.actions.raiseHand
import com.school.fold
import com.school.map
import com.school.model.Lesson
import com.school.model.LessonState
import com.school.model.LoadedLesson
import com.school.model.toRuntime
import com.school.service.LessonService

fun <F> runLesson(loadedLesson: LoadedLesson,
                  concurrent: Concurrent<F>): Kind<F, LessonService<F>> {
    val lesson = loadedLesson.toRuntime()
    return concurrent.fx.concurrent {
        val ref = Ref(lesson).bind()
        val lessonService: LessonService<F> = object : LessonService<F> {
            override fun lesson(): Kind<F, Result<Lesson>> =
                    ref.get().map { Result.pure(it) }

            override fun joinLesson(participantId: Int, name: String): Kind<F, Result<Unit>> =
                    run(ref) { it.join(participantId, name) }

            override fun raiseHand(participantId: Int): Kind<F, Result<Unit>> =
                run(ref) { it.raiseHand(participantId) }

        }
        lessonService
    }
}

private fun <F, A> run(ref: Ref<F, Lesson>,
                       action: (Lesson) -> Result<LessonState<A>>): Kind<F, Result<A>> =
        ref.modify { lesson ->
            val updateResult = action(lesson)
            //TODO increment lesson version here?
            val newLesson = updateResult.fold({ lesson }, { it.lesson })
            Tuple2(newLesson, updateResult.map { it.a })
        }
