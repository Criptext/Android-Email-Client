package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.flatMapError

/**
 * Created by gabriel on 5/16/18.
 */

class CheckUsernameAvailabilityWorker(val httpClient: HttpClient,
                                      private val storage: KeyValueStorage,
                                      private val accountDao: AccountDao,
                                      private val username: String,
                                      private val domain: String,
                                      override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.CheckUsernameAvailability> {

    private val apiClient = SignUpAPIClient(httpClient)

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SignInResult.CheckUsernameAvailability {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.Gone -> SignInResult.CheckUsernameAvailability.Failure(UIMessage(R.string.username_not_available))
                ServerCodes.EnterpriseAccountSuspended ->
                    SignInResult.CheckUsernameAvailability.Failure(UIMessage(R.string.account_suspended_sign_in_error))
                ServerCodes.BadRequest ->
                    SignInResult.CheckUsernameAvailability.Failure(UIMessage(R.string.username_invalid_error))
                else -> SignInResult.CheckUsernameAvailability.Failure(UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode)))
            }
        } else {
            SignInResult.CheckUsernameAvailability.Failure(UIMessage(R.string.forgot_password_error))
        }
    }

    override fun work(reporter: ProgressReporter<SignInResult.CheckUsernameAvailability>): SignInResult.CheckUsernameAvailability? {

        val loggedUsers = accountDao.getLoggedInAccounts().map { it.recipientId.plus("@${it.domain}") }
        if(username.plus("@$domain") in loggedUsers) return SignInResult.CheckUsernameAvailability.Failure(UIMessage(R.string.user_already_logged_in))

        val result = Result.of { apiClient.userCanLogin(username, domain) }

        val loggedOutAccounts = AccountUtils.getLastLoggedAccounts(storage)
        loggedOutAccounts.removeAll(loggedUsers)
        storage.putString(KeyValueStorage.StringKey.LastLoggedUser, loggedOutAccounts.distinct().joinToString())

        return when (result) {
            is Result.Success -> SignInResult.CheckUsernameAvailability.Success(userExists = true, username = username, domain = domain)
            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}