package com.school.service

import arrow.Kind
import com.school.Result
import com.school.model.User

interface UserContainer<F> {
    fun registerUser(name: String, email: String, password: String): Kind<F, User>
}