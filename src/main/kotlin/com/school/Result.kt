package com.school

import arrow.fx.IO
import arrow.fx.typeclasses.Effect
import com.school.model.Notification

sealed class Result<out A> {
    abstract val notifications: List<Notification>

    data class Success<A>(val value: A, override val notifications: List<Notification>) : Result<A>()
    data class Error(val msg: String, override val notifications: List<Notification>) : Result<Nothing>()

    companion object {
        private val singleVoid: Success<Unit> = Success(Unit, listOf())

        fun <A> pure(a: A): Result<A> = Success(a, listOf())
        fun <A> pure(a: A, notifications: List<Notification>): Result<A> = Success(a, notifications)

        fun void(): Result<Unit> = singleVoid
        fun void(notifications: List<Notification>): Result<Unit> = Success(Unit, notifications)
    }
}

fun <A> Result<A>.appendNotifications(notifications: List<Notification>): Result<A> =
        when (this) {
            is Result.Success -> Result.Success(value, notifications + this.notifications)
            is Result.Error -> Result.Error(msg, notifications + this.notifications)
        }

fun <A, B> Result<A>.flatMap(f: (A) -> Result<B>): Result<B> =
        when (this) {
            is Result.Success -> f(value).appendNotifications(this.notifications)
            is Result.Error -> this
        }

fun <A, B> Result<A>.map(f: (A) -> B): Result<B> =
        flatMap { Result.Success(f(it), notifications) }

fun <A> Effect<A>.toResult(): Result<A> = TODO()

fun <A> IO<A>.toResult(): Result<A> = TODO()