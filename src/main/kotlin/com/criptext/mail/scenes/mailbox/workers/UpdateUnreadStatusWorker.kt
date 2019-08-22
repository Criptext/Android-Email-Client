package com.criptext.mail.scenes.mailbox.workers

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.batch
import com.criptext.mail.utils.peerdata.PeerThreadReadData
import com.github.kittinunf.result.Result

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateUnreadStatusWorker(
        private val db: MailboxLocalDB,
        private val pendingDao: PendingEventDao,
        private val threadIds: List<String>,
        private val updateUnreadStatus: Boolean,
        private val currentLabel: Label,
        private val activeAccount: ActiveAccount,
        httpClient: HttpClient,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        override val publishFn: (MailboxResult.UpdateUnreadStatus) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateUnreadStatus> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao,
            storage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateUnreadStatus =
        if(ex is ServerErrorException) {
            when {
                ex.errorCode == ServerCodes.Unauthorized ->
                    MailboxResult.UpdateUnreadStatus.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerCodes.Forbidden ->
                    MailboxResult.UpdateUnreadStatus.Forbidden()
                else -> MailboxResult.UpdateUnreadStatus.Failure(createErrorMessage(ex))
            }
        }
        else MailboxResult.UpdateUnreadStatus.Failure(createErrorMessage(ex))


    override fun work(reporter: ProgressReporter<MailboxResult.UpdateUnreadStatus>)
            : MailboxResult.UpdateUnreadStatus? {
        val result = Result.of {
            val defaultLabels = Label.DefaultItems()
            val rejectedLabels = defaultLabels.rejectedLabelsByMailbox(currentLabel).map { it.id }
            db.updateUnreadStatus(threadIds, updateUnreadStatus, rejectedLabels, activeAccount.id)
        }

        return when (result) {
            is Result.Success -> {
                threadIds.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach { batch ->
                    peerEventHandler.enqueueEvent(PeerThreadReadData(batch, updateUnreadStatus).toJSON())
                }
                MailboxResult.UpdateUnreadStatus.Success(threadId = threadIds, unreadStatus = updateUnreadStatus)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            is NetworkErrorException -> UIMessage(resId = R.string.error_updating_status)
            else -> UIMessage(resId = R.string.error_updating_status)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}

