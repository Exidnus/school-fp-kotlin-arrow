package com.school.storage.impl

import arrow.Kind
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.MonadDefer
import com.school.model.UserId
import com.school.storage.UserStorage

class UserStorageImpl<F>(private val md: MonadDefer<F>) : UserStorage<F> {
    override fun addUser(name: String, email: String, password: String): Kind<F, Int> =
            md.later {
                println("saving")
                42
            } // save to db by not fp library

    override fun getUserName(userId: UserId): Kind<F, String> {
        TODO("Not yet implemented")
    }
}