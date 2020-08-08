package com.school.storage.aggregator

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.monad.monad
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.milliseconds
import arrow.fx.typeclasses.seconds
import arrow.unsafe
import com.school.model.Notification

/*
fun main() {
    val storage: UserStorage<ForIO> = UserStorageImpl(io)
    val userService: UserContainer<ForIO> = UserContainerImpl(storage, io)
    val result = userService.registerUser("vasya", "example@ya.ru", "12345")
    println(result)
    val completed = unsafe {
        runBlocking {
            result
        }
    }
    println(completed)
}
 */


val concurrent: Concurrent<ForIO> = IO.concurrent()

fun main() {
    val aggregator = createBatchAggregatorActor(
            notebookId = 1,
            pageId = 1,
            batchSizeLimit = 10,
            insertTimeout = 100L.milliseconds,
            saveBatchToDb = { _, _, array ->  concurrent.effect { println("saving ${array.size}") } },
            serialize = { concurrent.effect { ByteArray(size = it.size) } },
            concurrent = IO.concurrent()
    )

    val runAggregator = concurrent.fx.concurrent {
        val aggr = aggregator.bind()
        aggr.addNotify(Notification.StartPaint("line")).bind()
        concurrent.sleep(200L.milliseconds).bind()
        aggr.addNotify(Notification.StartPaint("line")).bind()
        concurrent.sleep(200L.milliseconds).bind()
        aggr.addNotify(Notification.StartPaint("line")).bind()
        concurrent.sleep(200L.milliseconds).bind()
        aggr.addNotify(Notification.StartPaint("line")).bind()
        concurrent.sleep(200L.milliseconds).bind()
        aggr.addNotify(Notification.StartPaint("line")).bind()
        concurrent.sleep(200L.milliseconds).bind()
        aggr.addNotify(Notification.StartPaint("line")).bind()
        concurrent.sleep(20L.seconds).bind()
    }

    unsafe {
        runBlocking {
            runAggregator
        }
    }
}