package com.school.service.impl

import arrow.Kind
import arrow.fx.*
import arrow.fx.typeclasses.Concurrent
import arrow.typeclasses.Monad
import com.school.algebra.async
import com.school.model.Lesson
import com.school.model.LoadedLesson
import com.school.model.toRuntime
import com.school.service.LessonService

fun <F> runLesson(loadedLesson: LoadedLesson,
                  concurrent: Concurrent<F>): Kind<F, LessonService<F>> {
    val lesson = loadedLesson.toRuntime()

    concurrent.fx.concurrent {
        val queueToActor = Queue.unbounded<Message<F>>().bind()
        val lessonRef = Ref(lesson).bind()
        val lessonActor = LessonActor(
                queueToActor,
                lessonRef,
                concurrent
        )
        lessonActor.run().async(concurrent).bind()
    }
}

private class LessonActor<F>(private val incoming: Queue<F, Message<F>>,
                             private val lessonRef: Ref<F, Lesson>,
                             private val concurrent: Concurrent<F>) : Monad<F> by concurrent {
    fun run(): Kind<F, Unit> =
        concurrent.fx
                .monad {
                    when (val msg = incoming.take().bind()) {
                        is Message.Action<F> -> processAction(msg)
                    }
                }
                .repeat(concurrent, Schedule.forever(concurrent))
                .void()

    private fun processAction(action: Message.Action<F>): Kind<F, Unit> =
            concurrent.fx.monad {
                val lessonState = lessonRef.get().bind()
                // TODO increment model version somehow
                val newLessonState = action.run(lessonState).bind()
                lessonRef.set(newLessonState).bind()
                action.promise.complete(newLessonState).bind()
            }

}

private sealed class Message<F> {
    data class Action<F>(val run: (Lesson) -> Kind<F, Lesson>,
                         val promise: Promise<F, Lesson>) : Message<F>()
}