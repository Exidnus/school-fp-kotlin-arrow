package com.school.service.impl

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.fx.typeclasses.Concurrent
import arrow.unsafe
import com.school.model.LoadedLesson
import com.school.model.LoadedParticipant
import java.time.Duration
import java.time.Instant


val io: Concurrent<ForIO> = IO.concurrent()

fun main() {
    val loadedLesson = LoadedLesson(1,
            teacher = LoadedParticipant(1, "teacher"),
            students = listOf(),
            subject = "subject",
            description = "description",
            beginTime = Instant.now(),
            endTime = Instant.now() + Duration.ofHours(1))

    val effect = io.fx.concurrent {
        val lessonService = runLesson(loadedLesson, io).bind()
        val join = lessonService.joinLesson(2, "student1").bind()
        io.effect { println(join) }.bind()
        val lessonInfo = lessonService.lesson().bind()
        io.effect { println(lessonInfo) }.bind()
    }

    unsafe {
        runBlocking {
            effect
        }
    }
}