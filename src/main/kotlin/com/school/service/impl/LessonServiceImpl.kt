package com.school.service.impl

import arrow.Kind
import arrow.fx.Ref
import arrow.fx.Semaphore
import arrow.fx.typeclasses.Concurrent
import com.school.Result
import com.school.fold
import com.school.map
import com.school.model.Lesson
import com.school.model.LessonState

//fun <F> runLesson(loadedLesson: LoadedLesson,
//                  concurrent: Concurrent<F>): Kind<F, LessonService<F>> {
//    val lesson = loadedLesson.toRuntime()
//    return concurrent.fx.concurrent {
//        val lessonRunner = LessonRunner(
//                Ref(lesson).bind(),
//                Semaphore(1).bind(),
//                concurrent
//        )
//        val lessonService: LessonService<F> = object : LessonService<F> {
//            override fun lesson(): Kind<F, Result<Lesson>> =
//                    ref.get().map { Result.pure(it) }
//
//            override fun joinLesson(participantId: Int, name: String): Kind<F, Result<Unit>> =
//                    run(ref) { it.join(participantId, name) }
//
//            override fun raiseHand(participantId: Int): Kind<F, Result<Unit>> =
//                    run(ref) { it.raiseHand(participantId) }
//
//        }
//        lessonService
//    }
//}

private class LessonRunner<F>(private val ref: Ref<F, Lesson>,
                              private val lock: Semaphore<F>,
                              private val concurrent: Concurrent<F>) {
    private fun <A> run(action: (Lesson) -> Kind<F, Result<LessonState<A>>>): Kind<F, Result<A>> {
        val refAction = concurrent.fx.concurrent {
            val lesson = ref.get().bind()
            val updateResult = action(lesson).bind()
            val newLesson = updateResult.fold({ lesson }, { it.lesson })
            ref.set(newLesson).bind()
            updateResult.map { it.a }
        }

        return lock.withPermit(refAction)
    }

}

