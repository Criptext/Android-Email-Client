package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.UIMessage
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
        val message = createErrorMessage(ex)
        return EmailDetailResult.ReadEmails.Failure(message)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.ReadEmails>)
            : EmailDetailResult.ReadEmails? {
        val emails = dao.getAllEmailsByMetadataKey(metadataKeys)
        val unreadEmails = emails.filter { it.unread }
        if(unreadEmails.isEmpty()) return EmailDetailResult.ReadEmails.Failure(UIMessage(R.string.nothing_to_update))
        val result = Result.of { apiClient.postOpenEvent(metadataKeys) }
                .flatMap { Result.of { apiClient.postThreadReadChangedEvent(unreadEmails.map { it.threadId }.distinct(),false) }}
                .flatMap { Result.of { dao.toggleCheckingRead(ids = unreadEmails.map { it.id }, unread = false) } }

        return when(result){
            is Result.Success -> EmailDetailResult.ReadEmails.Success(unreadEmails.size)
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> {
                when {
                    ex.errorCode == 401 -> UIMessage(resId = R.string.device_removed_remotely_exception)
                    else -> UIMessage(resId = R.string.server_error_exception)
                }
            }
            else -> UIMessage(resId = R.string.failed_getting_emails)
        }
    }
}