package com.school.storage

import arrow.Kind
import arrow.fx.IO

interface UserStorage<F> {
    fun addUser(name: String, email: String, password: String): Kind<F, Int>
}