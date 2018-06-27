package com.email.scenes.emaildetail.workers

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.EmailDao
import com.email.db.models.ActiveAccount
import com.email.scenes.emaildetail.data.EmailDetailAPIClient
import com.email.scenes.emaildetail.data.EmailDetailResult

/**
 * Created by gabriel on 6/27/18.
 */
class ReadEmailsWorker(private val dao: EmailDao,
                       httpClient: HttpClient,
                       activeAccount: ActiveAccount,
                       override val publishFn: (EmailDetailResult.ReadEmails) -> Unit,
                       private val emailIds: List<Long>,
                       private val metadataKeys: List<Long>
                       ) : BackgroundWorker<EmailDetailResult.ReadEmails> {

    override val canBeParallelized = false
    private val apiClient = EmailDetailAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EmailDetailResult.ReadEmails {
        // the user doesn't need to know if this fails so return empty object
        return EmailDetailResult.ReadEmails.Failure()
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.ReadEmails>)
            : EmailDetailResult.ReadEmails? {
        dao.toggleRead(ids = emailIds, unread = false)
        apiClient.postOpenEvent(metadataKeys)

        return EmailDetailResult.ReadEmails.Success()
    }

    override fun cancel() {
        TODO("not implemented")
    }
}