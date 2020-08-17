package com.school.storage

import arrow.Kind
import arrow.fx.IO
import com.school.model.UserId

interface UserStorage<F> {
    fun addUser(name: String, email: String, password: String): Kind<F, Int>

    fun getUserName(userId: UserId): Kind<F, String>
}