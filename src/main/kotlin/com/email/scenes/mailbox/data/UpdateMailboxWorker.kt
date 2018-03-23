package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val label: String,
        override val publishFn: (
                MailboxResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMailbox> {

    private val apiClient = MailboxAPIClient(activeAccount.jwt)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateMailbox {

        val message = createErrorMessage(ex)
        return MailboxResult.UpdateMailbox.Failure(label, message)
    }

    override fun work(): MailboxResult.UpdateMailbox? {
        val operationResult =  Result.of {
            apiClient.getPendingEvents()
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when(operationResult) {
            is Result.Success ->
                    TODO("SUCESS RESULT -> GET THE EVENTS")
/*                MailboxResult.UpdateMailbox.Success(
                )*/
            is Result.Failure -> MailboxResult.UpdateMailbox.Failure(
                    label, createErrorMessage(operationResult.error))
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
