package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.services.data.MessagingServiceController
import com.criptext.mail.services.data.MessagingServiceDataSource
import com.criptext.mail.signal.SignalKeyGenerator

/**
 * Created by sebas on 2/15/18.
 */

class SignInDataSource(override val runner: WorkRunner,
                       private val httpClient: HttpClient,
                       private val keyGenerator: SignalKeyGenerator,
                       private val keyValueStorage: KeyValueStorage,
                       private val signUpDao: SignUpDao,
                       private val accountDao: AccountDao,
                       private val signInLocalDB: SignInLocalDB)
    : BackgroundWorkManager<SignInRequest, SignInResult>() {
    override fun createWorkerFromParams(params: SignInRequest,
                                        flushResults: (SignInResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is SignInRequest.AuthenticateUser -> AuthenticateUserWorker(
                    db = signUpDao,
                    httpClient = httpClient,
                    username = params.username,
                    password = params.password,
                    keyGenerator = keyGenerator,
                    keyValueStorage = keyValueStorage,
                    accountDao = accountDao,
                    messagingInstance = MessagingInstance.Default(),
                    publishFn = { result ->
                        flushResults(result)
                    })

            is SignInRequest.VerifyUser -> VerifyUserWorker(signInLocalDB,
                    username = params.username,
                    publishFn = { result ->
                        flushResults(result)
                    })

            is SignInRequest.CheckUserAvailability -> CheckUsernameAvailabilityWorker(
                    httpClient = httpClient,
                    username = params.username,
                    publishFn = { result -> flushResults(result)
                    })
        }
    }
}
