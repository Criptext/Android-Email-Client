package com.email.scenes.search.data

import com.email.scenes.mailbox.data.LoadParams
import com.email.db.models.Label
import com.email.scenes.mailbox.data.EmailThread

sealed class SearchRequest{

    data class SearchEmails(
            val queryText: String,
            val loadParams: LoadParams
    ): SearchRequest()

    data class UpdateUnreadStatus(val emailThreads: List<EmailThread>,
                                  val updateUnreadStatus: Boolean,
                                  val currentLabel: Label): SearchRequest()

}