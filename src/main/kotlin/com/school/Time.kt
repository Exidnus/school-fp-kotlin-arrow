package com.school

import arrow.Kind
import java.time.Instant

interface Time<F> {
    fun now(): Kind<F, Instant>
}