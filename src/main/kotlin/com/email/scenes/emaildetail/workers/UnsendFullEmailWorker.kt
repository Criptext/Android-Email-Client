package com.email.scenes.emaildetail.workers

import com.email.R
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.ContactTypes
import com.email.db.EmailDetailLocalDB
import com.email.db.dao.EmailContactJoinDao
import com.email.db.dao.EmailDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.scenes.emaildetail.data.EmailDetailAPIClient
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.utils.EmailAddressUtils
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError

/**
 * Created by sebas on 3/22/18.
 */


class UnsendFullEmailWorker(
        private val db: EmailDetailLocalDB,
        private val emailDao: EmailDao,
        private val emailContactDao: EmailContactJoinDao,
        private val emailId: Long,
        private val position: Int,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (EmailDetailResult.UnsendFullEmailFromEmailId) -> Unit)
    : BackgroundWorker<EmailDetailResult.UnsendFullEmailFromEmailId> {

    private val apiClient = EmailDetailAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception):
            EmailDetailResult.UnsendFullEmailFromEmailId {

        val message = createErrorMessage(ex)
        return EmailDetailResult.UnsendFullEmailFromEmailId.
                Failure(position, message, ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.UnsendFullEmailFromEmailId>)
            : EmailDetailResult.UnsendFullEmailFromEmailId {

        val unsentEmail = emailDao.findEmailById(emailId)
        val result = Result.of {
            apiClient.postUnsendEvent(unsentEmail!!.metadataKey, getMailRecipients(unsentEmail))
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when (result) {
            is Result.Success -> {
                db.unsendEmail(emailId)
                EmailDetailResult.UnsendFullEmailFromEmailId.Success(position)
            }
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                EmailDetailResult.UnsendFullEmailFromEmailId.Failure(position, message, result.getException())
            }
        }
    }

    override fun cancel() {
    }

    private fun getMailRecipients(email: Email): List<String> {
        val contactsCC = emailContactDao.getContactsFromEmail(email.id, ContactTypes.CC).map { it.email }
        val contactsBCC = emailContactDao.getContactsFromEmail(email.id, ContactTypes.BCC).map { it.email }
        val contactsTO = emailContactDao.getContactsFromEmail(email.id, ContactTypes.TO).map { it.email }

        val toCriptext = contactsTO.filter(EmailAddressUtils.isFromCriptextDomain)
        val ccCriptext = contactsCC.filter(EmailAddressUtils.isFromCriptextDomain)
        val bccCriptext = contactsBCC.filter(EmailAddressUtils.isFromCriptextDomain)

        return toCriptext + ccCriptext + bccCriptext
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.fail_unsend_email)
    }
}
