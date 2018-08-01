package com.criptext.mail.utils

import com.criptext.mail.db.dao.EmailInsertionDao
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