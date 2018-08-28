package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import org.json.JSONObject

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateUnreadStatusWorker(
        private val db: EmailDetailLocalDB,
        private val threadId: String,
        private val updateUnreadStatus: Boolean,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        private val currentLabel: Label,
        override val publishFn: (EmailDetailResult.UpdateUnreadStatus) -> Unit)
    : BackgroundWorker<EmailDetailResult.UpdateUnreadStatus> {

    private val apiClient = EmailDetailAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.UpdateUnreadStatus =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerErrorCodes.Unauthorized ->
                    EmailDetailResult.UpdateUnreadStatus.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    EmailDetailResult.UpdateUnreadStatus.Forbidden()
                else -> EmailDetailResult.UpdateUnreadStatus.Failure(createErrorMessage(ex))
            }
        }
        else EmailDetailResult.UpdateUnreadStatus.Failure(createErrorMessage(ex))

    private fun updateUnreadEmailStatus() =
        Result.of {
            val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
            val emailIds = db.getFullEmailsFromThreadId(threadId, rejectedLabels).map {
                it.email.id
            }
            db.updateUnreadStatus(emailIds, updateUnreadStatus)
        }


    override fun work(reporter: ProgressReporter<EmailDetailResult.UpdateUnreadStatus>): EmailDetailResult.UpdateUnreadStatus? {
        val result = Result.of { apiClient.postThreadReadChangedEvent(listOf(threadId),
                updateUnreadStatus)}
                .flatMap { updateUnreadEmailStatus()}
        return when (result) {
            is Result.Success -> {
                EmailDetailResult.UpdateUnreadStatus.Success(threadId, updateUnreadStatus)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> UIMessage(resId = R.string.server_error_exception)
            else -> UIMessage(resId = R.string.error_updating_status)
        }
    }
}

