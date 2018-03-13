package com.email.scenes.emaildetail.data

import com.email.db.models.FullEmail
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/12/18.
 */

sealed class EmailDetailResult {

    sealed class LoadFullEmailsFromThreadId: EmailDetailResult() {
        data class Success(val fullEmailList: List<FullEmail>): LoadFullEmailsFromThreadId()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): LoadFullEmailsFromThreadId()
    }
}
