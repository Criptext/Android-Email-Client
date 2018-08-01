package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult

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