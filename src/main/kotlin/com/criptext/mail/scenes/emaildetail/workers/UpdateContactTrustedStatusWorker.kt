package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.PeerContactTrustedChanged
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerOpenEmailData
import com.criptext.mail.utils.peerdata.PeerThreadReadData
import com.github.kittinunf.result.Result

class UpdateContactTrustedStatusWorker(
        private val db: EmailDetailLocalDB,
        val pendingEventDao: PendingEventDao,
        val accountDao: AccountDao,
        private val newIsTrusted: Boolean,
        private val contact: Contact,
        private val metadataKey: Long,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        val storage: KeyValueStorage,
        override val publishFn: (EmailDetailResult.UpdateContactIsTrusted) -> Unit)
    : BackgroundWorker<EmailDetailResult.UpdateContactIsTrusted> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingEventDao,
            storage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.UpdateContactIsTrusted =
        EmailDetailResult.UpdateContactIsTrusted.Failure(createErrorMessage(ex), ex)


    override fun work(reporter: ProgressReporter<EmailDetailResult.UpdateContactIsTrusted>): EmailDetailResult.UpdateContactIsTrusted? {
        val result =  Result.of {
            peerEventHandler.enqueueEvent(PeerContactTrustedChanged(contact.email, newIsTrusted).toJSON())
            db.updateContactIsTrusted(contact, newIsTrusted)
        }
        return when (result) {
            is Result.Success -> {
                EmailDetailResult.UpdateContactIsTrusted.Success(contact, metadataKey, newIsTrusted)
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
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.error_updating_status)
        }
    }
}

