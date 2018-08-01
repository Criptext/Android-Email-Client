package com.criptext.mail.scenes.search.data

import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.EmailThread

sealed class SearchRequest{

    data class SearchEmails(
            val userEmail: String,
            val queryText: String,
            val loadParams: LoadParams
    ): SearchRequest()

    data class UpdateUnreadStatus(val emailThreads: List<EmailThread>,
                                  val updateUnreadStatus: Boolean,
                                  val currentLabel: Label): SearchRequest()

}