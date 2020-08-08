package com.school.storage

import arrow.Kind

interface PageStorage<F> {
    fun addNotifyBatch(notebookId: Int, pageId: Int, batch: ByteArray): Kind<F, Unit>
}