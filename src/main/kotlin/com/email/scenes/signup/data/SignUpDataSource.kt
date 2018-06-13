package com.email.scenes.signup.data

import com.email.api.HttpClient
import com.email.signal.SignalKeyGenerator
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.KeyValueStorage
import com.email.db.dao.SignUpDao

/**
 * Created by sebas on 2/15/18.
 */

class SignUpDataSource(override val runner: WorkRunner,
                       private val httpClient: HttpClient,
                       private val db: SignUpDao,
                       private val signalKeyGenerator: SignalKeyGenerator,
                       private val keyValueStorage: KeyValueStorage )
    : BackgroundWorkManager<SignUpRequest, SignUpResult>() {
    override fun createWorkerFromParams(params: SignUpRequest,
                                        flushResults: (SignUpResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is SignUpRequest.RegisterUser -> RegisterUserWorker(
                    db = db,
                    httpClient = httpClient,
                    incompleteAccount = params.account,
                    signalKeyGenerator = signalKeyGenerator,
                    keyValueStorage = keyValueStorage,
                    publishFn = { result ->
                flushResults(result)
            })
            is SignUpRequest.CheckUserAvailability -> CheckUsernameAvailabilityWorker(
                    httpClient = httpClient,
                    username = params.username,
                    publishFn = { result -> flushResults(result)
            })
        }
    }
}
