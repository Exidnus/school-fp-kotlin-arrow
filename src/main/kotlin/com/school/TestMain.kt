package com.school

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.fx.typeclasses.Concurrent
import arrow.unsafe
import com.school.service.UserContainer
import com.school.service.impl.UserContainerImpl
import com.school.storage.UserStorage
import com.school.storage.impl.UserStorageImpl

val io: Concurrent<ForIO> = IO.concurrent()

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