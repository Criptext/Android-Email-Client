package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerReadEmailData
import com.github.kittinunf.result.Result


class MarkAsReadEmailWorker(private val dao: EmailDao,
                            pendingDao: PendingEventDao,
                            accountDao: AccountDao,
                            storage: KeyValueStorage,
                            httpClient: HttpClient,
                            private val activeAccount: ActiveAccount,
                            override val publishFn: (EmailDetailResult.MarkAsReadEmail) -> Unit,
                            private val metadataKeys: List<Long>,
                            private val threadId: String,
                            private val unread: Boolean
                       ) : BackgroundWorker<EmailDetailResult.MarkAsReadEmail> {

    override val canBeParallelized = false
    private val peerEventHandler = PeerEventsApiHandler.Default(activeAccount, pendingDao,
            storage, accountDao)


    override fun catchException(ex: Exception): EmailDetailResult.MarkAsReadEmail {
        val message = createErrorMessage(ex)
        return EmailDetailResult.MarkAsReadEmail.Failure(message)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.MarkAsReadEmail>)
            : EmailDetailResult.MarkAsReadEmail? {
        val result = Result.of {
            val peerEmail = dao.getAllEmailsByMetadataKey(metadataKeys, activeAccount.id)
                    .filter { it.delivered !in listOf(DeliveryTypes.FAIL, DeliveryTypes.SENDING)}
            if(peerEmail.isEmpty())
                throw Exception()
            peerEventHandler.enqueueEvent(PeerReadEmailData(metadataKeys, unread).toJSON())
            dao.toggleReadByMetadataKey(metadataKeys = metadataKeys,
                    unread = unread, accountId = activeAccount.id)
        }

        return when (result) {
            is Result.Success -> {
                EmailDetailResult.MarkAsReadEmail.Success(metadataKeys, threadId, unread)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
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