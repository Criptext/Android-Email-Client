package com.email.scenes.mailbox.data

import com.email.db.models.Label
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxResult {

    sealed class GetLabels : MailboxResult() {
        class Success(val labels: List<Label>,
                      val defaultSelectedLabels: List<Label>): GetLabels()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : GetLabels()
    }
}

