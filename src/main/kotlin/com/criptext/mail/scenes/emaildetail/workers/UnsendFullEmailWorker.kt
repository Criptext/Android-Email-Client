package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
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
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
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
        val result = workOperation(unsentEmail)

        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(result)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation(unsentEmail)
        else
            result

        return when (finalResult) {
            is Result.Success -> {
                val date = db.unsendEmail(emailId)
                EmailDetailResult.UnsendFullEmailFromEmailId.Success(position, date)
            }
            is Result.Failure -> {
                catchException(finalResult.error)
            }
        }
    }

    override fun cancel() {
    }

    private fun workOperation(unsentEmail: Email?) : Result<String, Exception> = Result.of {
        apiClient.postUnsendEvent(unsentEmail!!.metadataKey,
                getMailRecipients(unsentEmail))
    }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun newRetryWithNewSessionOperation(unsentEmail: Email?)
            : Result<String, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, accountDao)
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                apiClient.authToken = account.jwt
                workOperation(unsentEmail)
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
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
