package com.school.storage.aggregator

import arrow.Kind
import arrow.fx.*
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.Duration
import arrow.typeclasses.Monad
import com.school.algebra.async
import com.school.model.NotebookId
import com.school.model.Notification
import com.school.model.PageId

fun <F> createBatchAggregatorActor(notebookId: NotebookId,
                                   pageId: PageId,
                                   batchSizeLimit: Int,
                                   insertTimeout: Duration,
                                   saveBatchToDb: (NotebookId, PageId, ByteArray) -> Kind<F, Unit>,
                                   serialize: (List<Notification>) -> Kind<F, ByteArray>,
                                   concurrent: Concurrent<F>): Kind<F, BatchAggregator<F>> =

        concurrent.fx.concurrent {
            val queueToActor = Queue.unbounded<Message>().bind()
            val stateRef = Ref(concurrent, State.empty).bind()

            val insertNotify = InsertNotify(
                    notebookId,
                    pageId,
                    batchSizeLimit,
                    saveBatchToDb,
                    serialize,
                    queueToActor,
                    stateRef,
                    concurrent
            )
            insertNotify.run().async(concurrent).bind()

            val insertTimer = InsertTimer(
                    queueToActor,
                    insertTimeout,
                    concurrent
            )
            insertTimer.run().async(concurrent).bind()

            val aggregator: BatchAggregator<F> = object : BatchAggregator<F> {
                override fun addNotify(notify: Notification): Kind<F, Unit> =
                        queueToActor.offer(Message.NewNotify(notify))

                override fun allNotify(): Kind<F, List<Notification>> =
                        stateRef.get().map { it.notify }
            }
            aggregator
        }

private class InsertTimer<F>(private val outgoing: Enqueue<F, Message>,
                             private val insertTimeout: Duration,
                             private val concurrent: Concurrent<F>) : Monad<F> by concurrent {

    fun run(): Kind<F, Unit> =
            concurrent.fx
                    .monad {
                        concurrent.sleep(insertTimeout).bind()
                        outgoing.offer(Message.InsertTimeout).bind()
                    }
                    //TODO use specific pool
                    .repeat(concurrent, Schedule.forever(concurrent))
                    .void()
}

private class InsertNotify<F>(private val notebookId: NotebookId,
                              private val pageId: PageId,
                              private val batchSizeLimit: Int,
                              private val saveBatchToDb: (NotebookId, PageId, ByteArray) -> Kind<F, Unit>,
                              private val serialize: (List<Notification>) -> Kind<F, ByteArray>,
                              private val incoming: Dequeue<F, Message>,
                              private val stateRef: Ref<F, State>,
                              private val concurrent: Concurrent<F>) : Monad<F> by concurrent {

    fun run(): Kind<F, Unit> =
            concurrent.fx
                    .monad {
                        val currentState = stateRef.get().bind()
                        val newState = when (val msg = incoming.take().bind()) {
                            is Message.NewNotify -> {
                                if (currentState.notify.size + 1 >= batchSizeLimit) {
                                    insert(currentState.notify + msg.notification).bind()
                                    State.empty
                                } else {
                                    currentState.add(msg.notification)
                                }
                            }
                            is Message.InsertTimeout -> {
                                if (currentState != State.empty)
                                    insert(currentState.notify).bind()
                                State.empty
                            }
                        }
                        stateRef.set(newState).bind()
                    }
                    //TODO use specific pool
                    .repeat(concurrent, Schedule.forever(concurrent))
                    .void()

    private fun insert(notify: List<Notification>): Kind<F, Unit> =
            concurrent.fx.monad {
                val serialized = serialize(notify).bind()
                saveBatchToDb(notebookId, pageId, serialized).bind()
            }
}

private sealed class Message {
    data class NewNotify(val notification: Notification) : Message()
    object InsertTimeout : Message()
}

private data class State(val notify: List<Notification>) {
    companion object {
        val empty: State = State(listOf())
    }
}

private fun State.add(notify: Notification): State = State(this.notify + notify)