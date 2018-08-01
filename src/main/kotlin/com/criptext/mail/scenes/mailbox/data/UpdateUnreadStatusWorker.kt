package com.criptext.mail.scenes.mailbox.data

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import org.json.JSONException

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateUnreadStatusWorker(
        private val db: MailboxLocalDB,
        private val threadIds: List<String>,
        private val updateUnreadStatus: Boolean,
        private val currentLabel: Label,
        activeAccount: ActiveAccount,
        httpClient: HttpClient,
        override val publishFn: (MailboxResult.UpdateUnreadStatus) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateUnreadStatus> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateUnreadStatus {
        val message = createErrorMessage(ex)
        return MailboxResult.UpdateUnreadStatus.Failure(message)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateUnreadStatus>)
            : MailboxResult.UpdateUnreadStatus? {
        val result = Result.of { apiClient.postThreadReadChangedEvent(threadIds,
                updateUnreadStatus)}
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when (result) {
            is Result.Success -> {
                val defaultLabels = Label.DefaultItems()
                val rejectedLabels = defaultLabels.rejectedLabelsByMailbox(currentLabel).map { it.id }
                db.updateUnreadStatus(threadIds, updateUnreadStatus, rejectedLabels)
                MailboxResult.UpdateUnreadStatus.Success()
            }
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                MailboxResult.UpdateUnreadStatus.Failure(message)
            }
        }
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is NetworkErrorException -> UIMessage(resId = R.string.error_updating_status)
            else -> UIMessage(resId = R.string.error_updating_status)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}

