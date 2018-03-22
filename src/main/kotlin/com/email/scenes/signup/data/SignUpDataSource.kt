package com.email.scenes.signup.data

import com.email.signal.SignalKeyGenerator
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner
import com.email.db.KeyValueStorage
import com.email.db.SignUpLocalDB

/**
 * Created by sebas on 2/15/18.
 */

class SignUpDataSource(override val runner: WorkRunner,
                       private val signUpAPIClient: SignUpAPIClient,
                       private val signUpLocalDB: SignUpLocalDB,
                       private val signalKeyGenerator: SignalKeyGenerator,
                       private val keyValueStorage: KeyValueStorage )
    : WorkHandler<SignUpRequest, SignUpResult>() {
    override fun createWorkerFromParams(params: SignUpRequest,
                                        flushResults: (SignUpResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is SignUpRequest.RegisterUser -> RegisterUserWorker(
                    db = signUpLocalDB,
                    apiClient = signUpAPIClient,
                    incompleteAccount = params.account,
                    signalKeyGenerator = signalKeyGenerator,
                    keyValueStorage = keyValueStorage,
                    publishFn = { result ->
                flushResults(result)
            })
        }
    }
}
