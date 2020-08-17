package com.school

import arrow.core.Option
import com.school.model.Notification

//interface Result<F> : Monad<F> {
//
//    override val fx: ResultFx<F>
//
//    fun <A> A.pure(): Kind<F, A>
//    fun <A> ErrorMsg.raiseError(): Kind<F, A>
//
//    fun Kind<F, *>.addNotify(notify: List<Notification>): Kind<F, Unit>
//    fun Kind<F, *>.addNotify(notify: Notification): Kind<F, Unit> = addNotify(listOf(notify))
//}
//
//interface ResultFx<F> : MonadFx<F> {
//    val R: Result<F>
//    override val M: Monad<F>
//        get() = R
//    fun <A> result(c: suspend ResultSyntax<F>.() -> A): Kind<F, A> = TODO()
//}
//
//interface ResultSyntax<F> : Result<F>, BindSyntax<F>
//
//enum class ErrorMsg {
//    PARTICIPANT_NOT_FOUND
//}

sealed class Result<out A> {
    abstract val notifications: List<Notification>

    data class Success<A>(val value: A, override val notifications: List<Notification>) : Result<A>() {
        constructor(value: A) : this(value, listOf())
    }
    data class Error(val msg: ErrorMsg, override val notifications: List<Notification>) : Result<Nothing>() {
        constructor(msg: ErrorMsg) : this(msg, listOf())
    }

    companion object {
        private val singleVoid: Success<Unit> = Success(Unit, listOf())

        fun <A> pure(a: A): Result<A> = Success(a)
        fun <A> pure(a: A, notifications: List<Notification>): Result<A> = Success(a, notifications)

        fun void(): Result<Unit> = singleVoid
        fun void(notifications: List<Notification>): Result<Unit> = Success(Unit, notifications)

        fun successIf(cond: Boolean, ifFalse: ErrorMsg): Result<Unit> =
                if (cond) void() else Error(ifFalse)
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

fun <A, B> Result<A>.fold(ifError: () -> B, ifSuccess: (A) -> B): B =
        when (this) {
            is Result.Success -> ifSuccess(this.value)
            is Result.Error -> ifError()
        }

fun <A> Option<A>.toResult(ifEmpty: ErrorMsg): Result<A> =
        this.fold( { Result.Error(ifEmpty) }, { Result.pure(it) } )

fun <A> A?.toResult(ifNull: ErrorMsg): Result<A> =
        if (this == null) Result.Error(ifNull) else Result.pure(this)
