package com.criptext.mail.scenes.signup.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.SignUpDao

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
