package com.email.scenes.signin.data

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.KeyValueStorage
import com.email.db.SignInLocalDB
import com.email.db.dao.SignUpDao
import com.email.signal.SignalKeyGenerator

/**
 * Created by sebas on 2/15/18.
 */

class SignInDataSource(override val runner: WorkRunner,
                       private val httpClient: HttpClient,
                       private val keyGenerator: SignalKeyGenerator,
                       private val keyValueStorage: KeyValueStorage,
                       private val signUpDao: SignUpDao,
                       private val signInLocalDB: SignInLocalDB)
    : WorkHandler<SignInRequest, SignInResult>() {
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
                    publishFn = { result ->
                        flushResults(result)
                    })

            is SignInRequest.VerifyUser -> VerifyUserWorker(signInLocalDB,
                    username = params.username,
                    publishFn = { result ->
                        flushResults(result)
                    })
        }
    }
}
