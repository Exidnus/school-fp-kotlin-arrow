package com.school.actions

import arrow.Kind
import arrow.fx.typeclasses.Concurrent


fun <F> Kind<F, Unit>.async(concurrent: Concurrent<F>): Kind<F, Unit> {
    val effect = this
    return concurrent.fx.concurrent {
        effect.fork().void().bind()
    }
}