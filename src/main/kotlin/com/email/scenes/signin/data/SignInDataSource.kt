package com.email.scenes.signin.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.SignInLocalDB

/**
 * Created by sebas on 2/15/18.
 */

class SignInDataSource(override val runner: WorkRunner,
                       private val signInAPIClient: SignInAPIClient,
                       private val signInLocalDB: SignInLocalDB)
    : WorkHandler<SignInRequest, SignInResult>() {
    override fun createWorkerFromParams(params: SignInRequest,
                                        flushResults: (SignInResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is SignInRequest.AuthenticateUser -> AuthenticateUserWorker(signInLocalDB,
                    signInAPIClient,
                    username = params.username,
                    password = params.password,
                    deviceId = params.deviceId,
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
