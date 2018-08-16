package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map

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
        val emails = dao.getAllEmailsByMetadataKey(metadataKeys)
        val unreadEmails = emails.filter { it.unread }
        val result = Result.of { apiClient.postOpenEvent(metadataKeys) }
                .flatMap { Result.of { apiClient.postThreadReadChangedEvent(unreadEmails.map { it.threadId }.distinct(),false) }}
                .flatMap { Result.of { dao.toggleCheckingRead(ids = unreadEmails.map { it.id }, unread = false) } }

        return when(result){
            is Result.Success -> EmailDetailResult.ReadEmails.Success(unreadEmails.size)
            is Result.Failure -> EmailDetailResult.ReadEmails.Failure()
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}