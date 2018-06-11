package com.email.scenes.search.data

import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.UIMessage

sealed class SearchResult{

    sealed class SearchEmails : SearchResult() {
        class Success(
                val emailThreads: List<EmailThread>,
                val isReset: Boolean,
                val queryText: String)
            : SearchEmails()
        data class Failure(val message: UIMessage) : SearchEmails()
    }

    sealed class UpdateUnreadStatus: SearchResult(){
        class Success: UpdateUnreadStatus()
        class Failure: UpdateUnreadStatus()
    }
}