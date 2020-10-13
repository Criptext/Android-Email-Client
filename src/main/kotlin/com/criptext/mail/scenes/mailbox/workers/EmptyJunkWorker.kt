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
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.batch
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result

class EmptyJunkWorker(
        private val db: MailboxLocalDB,
        private val pendingDao: PendingEventDao,
        private val isSpam: Boolean,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        override val publishFn: (
                MailboxResult.EmptyJunk) -> Unit)
    : BackgroundWorker<MailboxResult.EmptyJunk> {

    private val peerEventHandler = PeerEventsApiHandler.Default(activeAccount, pendingDao,
            storage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.EmptyJunk =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerCodes.Unauthorized ->
                        MailboxResult.EmptyJunk.Unauthorized(createErrorMessage(ex))
                    ex.errorCode == ServerCodes.SessionExpired ->
                        MailboxResult.EmptyJunk.SessionExpired()
                    ex.errorCode == ServerCodes.Forbidden ->
                        MailboxResult.EmptyJunk.Forbidden()
                    else -> MailboxResult.EmptyJunk.Failure(createErrorMessage(ex))
                }
            }
            else MailboxResult.EmptyJunk.Failure(createErrorMessage(ex))

    override fun work(reporter: ProgressReporter<MailboxResult.EmptyJunk>)
            : MailboxResult.EmptyJunk? {
        val metadataKeys = db.getEmailMetadataKeysFromLabel(if(isSpam) Label.LABEL_SPAM else Label.LABEL_TRASH, activeAccount.id)
        val result =  Result.of { db.deleteEmail(metadataKeys, activeAccount.id) }

        return when (result) {
            is Result.Success -> {
                metadataKeys.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach { batch ->
                    peerEventHandler.enqueueEvent(PeerDeleteEmailData(batch).toJSON())
                }
                MailboxResult.EmptyJunk.Success(isSpam)
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
