package com.email.utils

import com.email.db.dao.EmailInsertionDao
import io.mockk.CapturingSlot
import io.mockk.every

/**
 * Created by gabriel on 5/8/18.
 */

fun EmailInsertionDao.runTransactionsAsTheyAreInvoked() {
    val runnableSlot = CapturingSlot<Runnable>()
    every {
        this@runTransactionsAsTheyAreInvoked.runTransaction(capture(runnableSlot))
    } answers {
        runnableSlot.captured.run()
    }
}