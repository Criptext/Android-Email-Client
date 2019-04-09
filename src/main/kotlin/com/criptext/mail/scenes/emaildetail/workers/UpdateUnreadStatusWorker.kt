package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerThreadReadData
import com.github.kittinunf.result.Result

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateUnreadStatusWorker(
        private val db: EmailDetailLocalDB,
        private val pendingDao: PendingEventDao,
        private val threadId: String,
        private val updateUnreadStatus: Boolean,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        private val currentLabel: Label,
        override val publishFn: (EmailDetailResult.UpdateUnreadStatus) -> Unit)
    : BackgroundWorker<EmailDetailResult.UpdateUnreadStatus> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient,
            activeAccount, pendingDao, storage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.UpdateUnreadStatus =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    EmailDetailResult.UpdateUnreadStatus.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerCodes.Forbidden ->
                    EmailDetailResult.UpdateUnreadStatus.Forbidden()
                else -> EmailDetailResult.UpdateUnreadStatus.Failure(createErrorMessage(ex))
            }
        }
        else EmailDetailResult.UpdateUnreadStatus.Failure(createErrorMessage(ex))

    private fun updateUnreadEmailStatus() =
        Result.of {
            val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
            val emailIds = db.getFullEmailsFromThreadId(threadId = threadId,
                    rejectedLabels = rejectedLabels, account = activeAccount).map {
                it.email.id
            }
            db.updateUnreadStatus(emailIds, updateUnreadStatus, activeAccount.id)
        }


    override fun work(reporter: ProgressReporter<EmailDetailResult.UpdateUnreadStatus>): EmailDetailResult.UpdateUnreadStatus? {
        val result =  updateUnreadEmailStatus()
        return when (result) {
            is Result.Success -> {
                peerEventHandler.enqueueEvent(PeerThreadReadData(listOf(threadId), updateUnreadStatus).toJSON())
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

