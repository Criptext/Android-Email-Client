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
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerOpenEmailData
import com.criptext.mail.utils.peerdata.PeerReadEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap

/**
 * Created by gabriel on 6/27/18.
 */
class ReadEmailsWorker(private val dao: EmailDao,
                       private val pendingDao: PendingEventDao,
                       accountDao: AccountDao,
                       storage: KeyValueStorage,
                       httpClient: HttpClient,
                       private val activeAccount: ActiveAccount,
                       override val publishFn: (EmailDetailResult.ReadEmails) -> Unit,
                       private val emailIds: List<Long>,
                       private val metadataKeys: List<Long>
                       ) : BackgroundWorker<EmailDetailResult.ReadEmails> {

    override val canBeParallelized = false
    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao,
            storage, accountDao)


    override fun catchException(ex: Exception): EmailDetailResult.ReadEmails {
        val message = createErrorMessage(ex)
        return EmailDetailResult.ReadEmails.Failure(message)
    }

    private fun postOpenEmails(): Result<Int, Exception>{
        val emails = dao.getAllEmailsToOpenByMetadataKey(metadataKeys, activeAccount.id)
        val unreadEmails = emails.filter { it.unread }
        if(unreadEmails.isEmpty())
            return Result.Failure(EventHelper.NothingNewException())
        peerEventHandler.enqueueEvent(PeerOpenEmailData(emails.map { it.metadataKey }).toJSON())
        return Result.of {
                    dao.toggleCheckingRead(ids = unreadEmails.map { it.id },
                            unread = false, accountId = activeAccount.id)
                    unreadEmails.size
        }
    }

    private fun peerOpenEmails(): Result<Int, Exception>{
        val peerEmails = dao.getAllEmailsByMetadataKey(metadataKeys, activeAccount.id)
        val peerUnreadEmails = peerEmails.filter { it.unread }
        if(peerUnreadEmails.isEmpty())
            return Result.Failure(EventHelper.NothingNewException())
        val metadataKeys = peerUnreadEmails
                .filter { it.delivered !in listOf(DeliveryTypes.FAIL, DeliveryTypes.SENDING) }
                .map { it.metadataKey }
        peerEventHandler.enqueueEvent(PeerReadEmailData(metadataKeys,false).toJSON())
        return Result.of { dao.toggleCheckingRead(ids = peerUnreadEmails.map { it.id },
                            unread = false, accountId = activeAccount.id)
                    peerUnreadEmails.size
                }

    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.ReadEmails>)
            : EmailDetailResult.ReadEmails? {


        val resultPostOpen = postOpenEmails()
        val resultOpenPeers = peerOpenEmails()

        return when {
            resultPostOpen is Result.Failure
                    && resultOpenPeers is Result.Failure ->
                EmailDetailResult.ReadEmails.Failure(UIMessage(R.string.nothing_to_update))
            resultPostOpen is Result.Success
                    && resultOpenPeers is Result.Success ->
                EmailDetailResult.ReadEmails.Success(resultPostOpen.value
                        + resultOpenPeers.value)
            resultPostOpen is Result.Success
                    && resultOpenPeers is Result.Failure ->
                EmailDetailResult.ReadEmails.Success(resultPostOpen.value)
            resultOpenPeers is Result.Success
                    && resultPostOpen is Result.Failure ->
                EmailDetailResult.ReadEmails.Success(resultOpenPeers.value)
            else -> catchException(EventHelper.NothingNewException())
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