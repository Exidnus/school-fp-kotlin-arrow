package com.school.service.impl

import arrow.Kind
import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
import arrow.fx.*
import arrow.fx.typeclasses.Concurrent
import arrow.syntax.collections.tail
import arrow.typeclasses.Monad
import com.school.actions.async
import com.school.model.Lesson
import com.school.model.LessonId
import com.school.model.UserId
import com.school.service.LessonContainer
import com.school.storage.LessonStorage
import com.school.Result
import com.school.model.LessonState
import com.school.service.LessonService

//lessonStorage.addLesson(newLessonInfo)
fun <F> runLessonContainer(lessonStorage: LessonStorage<F>,
                           concurrent: Concurrent<F>): Kind<F, LessonContainer<F>> =
        concurrent.fx
                .concurrent {
                    val queueToActor = Queue.unbounded<Message<F>>().bind()
                    val lockWaitersRef = Ref(mapOf<LessonId, WaitersQueue<F>>()).bind()
                    val lockService = LockService(
                            queueToActor,
                            lockWaitersRef,
                            concurrent
                    )
                    lockService.run().async(concurrent).bind()

                    val lessonContainer: LessonContainer<F> = object : LessonContainer<F> {
                        override fun createLesson(newLessonInfo: LessonContainer.LessonInfo): Kind<F, Result<Int>> =
                                lessonStorage.addLesson(newLessonInfo)
                                        .map { Result.pure(it) }

                        override fun changeLesson(lessonId: LessonId, lessonInfo: LessonContainer.LessonInfo): Kind<F, Result<Unit>> {
                            TODO("Not yet implemented")
                        }

                        override fun joinLesson(lessonId: LessonId, userId: UserId): Kind<F, Result<Unit>> {
                            TODO("Not yet implemented")
                        }

                        override fun perform(f: (Lesson) -> Kind<F, Lesson>): Kind<F, Result<Unit>> {
                            TODO("Not yet implemented")
                        }
                    }
                    lessonContainer
                }

private class LessonServiceProvider<F>(inMemoryOrFromDb: Either<LessonService<F>, () -> LessonService<F>>) {
    fun <A> runWithLoadFromMemoryIfNeed(): Kind<F, Result<A>> = TODO()

    fun <A> runInMemoryOrInDbOnly(ifInMemory: (Lesson) -> Result<LessonState<A>>,
                                  ifInDbOnly: () -> Kind<F, Result<A>>): Kind<F, Result<A>> = TODO()
}

/*
1. Acquire, empty map -> add to map empty queue, complete promise from acquire
2. Acquire, map contains -> add promise to queue
3. Release, empty map -> remove entry from map
4. Release, map contains -> pull first promise and complete it, do not remove WaitersQueue if it's empty
 */
private class LockService<F>(private val queueToActor: Queue<F, Message<F>>,
                             private val lockWaitersRef: Ref<F, Map<LessonId, WaitersQueue<F>>>,
                             private val concurrent: Concurrent<F>) : Monad<F> by concurrent {
    fun run(): Kind<F, Unit> =
            concurrent.fx
                    .concurrent {
                        val newState = when (val msg = queueToActor.take().bind()) {
                            is Message.Acquire ->
                                handleAcquire(msg).bind()
                            is Message.Release ->
                                handleRelease(msg).bind()
                        }
                        lockWaitersRef.set(newState).bind()
                    }
                    .repeat(concurrent, Schedule.forever(concurrent))
                    .void()

    private fun handleAcquire(msg: Message.Acquire<F>): Kind<F, Map<LessonId, WaitersQueue<F>>> =
            concurrent.fx
                    .concurrent {
                        val state = lockWaitersRef.get().bind()
                        when (val waitersQueueOpt = state[msg.lessonId].toOption()) {
                            is Some -> {
                                state + Pair(msg.lessonId, waitersQueueOpt.t.offer(msg.promise))
                            }
                            is None -> {
                                msg.promise.complete(resource(msg.lessonId)).bind()
                                state + Pair(msg.lessonId, WaitersQueue.empty())
                            }
                        }
                    }

    private fun handleRelease(msg: Message.Release<F>): Kind<F, Map<LessonId, WaitersQueue<F>>> =
            concurrent.fx
                    .concurrent {
                        val state = lockWaitersRef.get().bind()
                        val waitersQueue = state.getValue(msg.lessonId)
                        if (waitersQueue.isEmpty()) {
                            state - msg.lessonId
                        } else {
                            val (next, tail) = waitersQueue.pull()
                            next.complete(resource(msg.lessonId)).bind()
                            state + Pair(msg.lessonId, tail)
                        }
                    }

    private fun resource(lessonId: LessonId): Resource<F, Throwable, Unit> =
            Resource(
                    acquire = { concurrent.just(Unit) },
                    release = { _ -> queueToActor.offer(Message.Release(lessonId)) },
                    BR = concurrent
            )
}

data class WaitersQueue<F>(private val queue: List<AcquirePromise<F>>) {
    companion object {
        fun <F> empty(): WaitersQueue<F> = WaitersQueue(listOf())
    }

    fun offer(promise: AcquirePromise<F>): WaitersQueue<F> =
            WaitersQueue(this.queue + promise)

    fun pull(): Pair<AcquirePromise<F>, WaitersQueue<F>> =
            Pair(queue.first(), WaitersQueue(queue.tail()))

    fun isEmpty(): Boolean = queue.isEmpty()
}

typealias AcquirePromise<F> = Promise<F, Resource<F, Throwable, Unit>>

private sealed class Message<F> {
    data class Acquire<F>(val lessonId: LessonId, val promise: AcquirePromise<F>) : Message<F>()
    data class Release<F>(val lessonId: LessonId) : Message<F>()
}