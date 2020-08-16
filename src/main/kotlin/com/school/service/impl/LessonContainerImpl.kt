package com.school.service.impl

import arrow.Kind
import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
import arrow.fx.*
import arrow.fx.typeclasses.Concurrent
import arrow.syntax.collections.tail
import com.school.model.Lesson
import com.school.model.LessonId
import com.school.model.UserId
import com.school.service.LessonContainer
import com.school.storage.LessonStorage
import java.lang.Exception

//lessonStorage.addLesson(newLessonInfo)
fun <F> runLessonContainer(lessonStorage: LessonStorage<F>,
                           concurrent: Concurrent<F>): Kind<F, LessonContainer<F>> {

}

/*
1. Acquire, empty map -> add to map empty queue, complete promise from acquire
2. Acquire, map contains -> add promise to queue
3. Release, empty map -> remove entry from map
4. Release, map contains -> pull first promise and complete it, do not remove WaitersQueue if it's empty
 */
private class LockService<F>(private val queueToActor: Dequeue<F, Message<F>>,
                             private val lockWaitersRef: Ref<F, Map<LessonId, WaitersQueue<F>>>,
                             private val concurrent: Concurrent<F>) {
    fun run(): Kind<F, Unit> {
        concurrent.fx.concurrent {
            val state = lockWaitersRef.get().bind()
            when (val msg = queueToActor.take().bind()) {
                is Message.Acquire -> {
                    when (val waitersQueueOpt = state[msg.lessonId].toOption()) {
                        is Some -> {
                            state + Pair(msg.lessonId, waitersQueueOpt.t.offer(msg.promise))
                        }
                        is None -> {
                            msg.promise.complete().bind()
                            state + Pair(msg.lessonId, WaitersQueue.empty())
                        }
                    }
                }
                is Message.Release -> {
                    val (remaining, next) = state.getValue(msg.lessonId).pull()
                    next.complete().bind()
                    if (remaining.isEmpty()) {
                        state - msg.lessonId
                    } else {
                        state + Pair(msg.lessonId, remaining)
                    }
                }
            }

        }
    }
}

//typealias WaitersQueue<F> = List<Promise<F, Resource<F, Exception, Unit>>>

data class WaitersQueue<F>(private val queue: List<Promise<F, Resource<F, Exception, Unit>>>) {
    companion object {
        fun <F> empty(): WaitersQueue<F> = WaitersQueue(listOf())
    }

    fun offer(promise: AcquirePromise<F>): WaitersQueue<F> =
            WaitersQueue(this.queue + promise)

    fun pull(): Pair<WaitersQueue<F>, AcquirePromise<F>> =
            Pair(WaitersQueue(queue.tail()), queue.first())

    fun isEmpty(): Boolean = queue.isEmpty()
}

typealias AcquirePromise<F> = Promise<F, Resource<F, Exception, Unit>>

private sealed class Message<F> {
    data class Acquire<F>(val lessonId: LessonId, val promise: AcquirePromise<F>) : Message<F>()
    data class Release<F>(val lessonId: LessonId) : Message<F>()
}