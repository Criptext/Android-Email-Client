package com.email.utils

import com.email.db.dao.EmailInsertionDao
import io.mockk.CapturingSlot
import io.mockk.every

/**
 * Created by gabriel on 5/8/18.
 */

fun EmailInsertionDao.runTransactionsAsTheyAreInvoked() {
    val lambdaSlot = CapturingSlot<() -> Long>()
    every {
        this@runTransactionsAsTheyAreInvoked.runTransaction(capture(lambdaSlot))
    } answers {
        lambdaSlot.captured()
    }
}