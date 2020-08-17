package com.school.storage

import arrow.fx.Resource
import arrow.fx.typeclasses.ExitCase
import arrow.fx.typeclasses.MonadDefer
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

//TODO use different pool for db operations
fun <F> tx(monadDefer: MonadDefer<F>): Resource<F, Throwable, Transaction> =
        Resource(
                acquire = {
                    monadDefer.later {
                        transaction {
                            this
                        }
                    }
                },
                release = { tx, exitCase ->
                    monadDefer.later {
                        if (exitCase == ExitCase.Completed)
                            tx.commit()
                        else
                            tx.rollback()
                    }
                },
                BR = monadDefer
        )