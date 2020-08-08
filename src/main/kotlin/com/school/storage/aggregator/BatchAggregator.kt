package com.school.storage.aggregator

import arrow.Kind
import com.school.model.Notification

interface BatchAggregator<F> {
    fun addNotify(notify: Notification): Kind<F, Unit>

    fun allNotify(): Kind<F, List<Notification>>
}