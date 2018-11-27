package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.dao.EmailContactJoinDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Email
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
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

        if(ex is ServerErrorException) {
            return when {
                ex.errorCode == ServerErrorCodes.Unauthorized ->
                    EmailDetailResult.UnsendFullEmailFromEmailId.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                ex.errorCode == ServerErrorCodes.Forbidden ->
                    EmailDetailResult.UnsendFullEmailFromEmailId.Forbidden()
                else -> {
                    val message = createErrorMessage(ex)
                    EmailDetailResult.UnsendFullEmailFromEmailId.Failure(position, message, ex)
                }
            }
        }else {
            val message = createErrorMessage(ex)
            return EmailDetailResult.UnsendFullEmailFromEmailId.Failure(position, message, ex)
        }
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.UnsendFullEmailFromEmailId>)
            : EmailDetailResult.UnsendFullEmailFromEmailId {

        val unsentEmail = emailDao.findEmailById(emailId)
        val result = Result.of {
            apiClient.postUnsendEvent(unsentEmail!!.metadataKey,
                    getMailRecipients(unsentEmail))
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

        return when (result) {
            is Result.Success -> {
                db.unsendEmail(emailId)
                EmailDetailResult.UnsendFullEmailFromEmailId.Success(position)
            }
            is Result.Failure -> {
                catchException(result.error)
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

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException ->  when(ex.errorCode) {
                ServerErrorCodes.MethodNotAllowed ->
                   UIMessage(resId = R.string.fail_unsend_email_expired)
                else -> UIMessage(resId = R.string.server_error_exception)
            }
            else -> UIMessage(resId = R.string.fail_unsend_email)
        }
    }
}
