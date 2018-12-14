package com.criptext.mail.scenes.mailbox.workers

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
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result

class EmptyTrashWorker(
        private val db: MailboxLocalDB,
        private val pendingDao: PendingEventDao,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        override val publishFn: (
                MailboxResult.EmptyTrash) -> Unit)
    : BackgroundWorker<MailboxResult.EmptyTrash> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao,
            storage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.EmptyTrash =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerErrorCodes.Unauthorized ->
                        MailboxResult.EmptyTrash.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                    ex.errorCode == ServerErrorCodes.Forbidden ->
                        MailboxResult.EmptyTrash.Forbidden()
                    else -> MailboxResult.EmptyTrash.Failure(createErrorMessage(ex))
                }
            }
            else MailboxResult.EmptyTrash.Failure(createErrorMessage(ex))

    override fun work(reporter: ProgressReporter<MailboxResult.EmptyTrash>)
            : MailboxResult.EmptyTrash? {
        val metadataKeys = db.getEmailMetadataKeysFromLabel(Label.LABEL_TRASH)
        val result =  Result.of { db.deleteEmail(metadataKeys) }

        return when (result) {
            is Result.Success -> {
                peerEventHandler.enqueueEvent(PeerDeleteEmailData(metadataKeys).toJSON())
                MailboxResult.EmptyTrash.Success()
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.failed_empty_trash)
        }
    }
}
