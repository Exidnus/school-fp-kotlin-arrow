package com.school.service.impl

import arrow.Kind
import arrow.typeclasses.Functor
import com.school.model.User
import com.school.service.UserContainer
import com.school.storage.UserStorage

class UserContainerImpl<F>(private val userStorage: UserStorage<F>,
                           private val functor: Functor<F>
) : UserContainer<F>, Functor<F> by functor {

    override fun registerUser(name: String, email: String, password: String): Kind<F, User> =
            userStorage.addUser(name, email, password)
                    .map { User(it, name, email, password) }
}