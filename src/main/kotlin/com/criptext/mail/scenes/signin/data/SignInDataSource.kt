package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.signal.SignalStoreCriptext
import java.io.File

/**
 * Created by sebas on 2/15/18.
 */

class SignInDataSource(override val runner: WorkRunner,
                       private val filesDir: File,
                       private val httpClient: HttpClient,
                       private val keyGenerator: SignalKeyGenerator,
                       private val keyValueStorage: KeyValueStorage,
                       private val signUpDao: SignUpDao,
                       private val accountDao: AccountDao,
                       private val signInLocalDB: SignInLocalDB,
                       private val db: AppDatabase)
    : BackgroundWorkManager<SignInRequest, SignInResult>() {
    override fun createWorkerFromParams(params: SignInRequest,
                                        flushResults: (SignInResult) -> Unit):
            BackgroundWorker<*> {
        return when (params) {
            is SignInRequest.AuthenticateUser -> AuthenticateUserWorker(
                    db = signInLocalDB,
                    signUpDao = signUpDao,
                    httpClient = httpClient,
                    username = params.username,
                    password = params.password,
                    keyGenerator = keyGenerator,
                    keyValueStorage = keyValueStorage,
                    accountDao = accountDao,
                    isMultiple = params.isMultiple,
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
            is SignInRequest.ForgotPassword -> ForgotPasswordWorker(
                    httpClient = httpClient, username = params.username,
                    publishFn = { result -> flushResults(result)
                    }
            )
            is SignInRequest.LinkBegin -> LinkBeginWorker(
                    httpClient = httpClient, username = params.username,
                    publishFn = { result -> flushResults(result)
                    }
            )
            is SignInRequest.LinkAuth -> LinkAuthWorker(
                    httpClient = httpClient, username = params.username,
                    jwt = params.ephemeralJwt, password = params.password,
                    publishFn = { result -> flushResults(result)
                    }
            )
            is SignInRequest.LinkStatus -> LinkStatusWorker(
                    httpClient = httpClient,
                    jwt = params.ephemeralJwt,
                    publishFn = { result -> flushResults(result)
                    }
            )
            is SignInRequest.CreateSessionFromLink -> CreateSessionWorker(
                    name = params.name,
                    httpClient = httpClient, randomId = params.randomId,
                    username = params.username, db = signInLocalDB,
                    accountDao = accountDao, ephemeralJwt = params.ephemeralJwt,
                    keyGenerator = keyGenerator, keyValueStorage = keyValueStorage,
                    messagingInstance = MessagingInstance.Default(),
                    signUpDao = signUpDao,
                    isMultiple = params.isMultiple,
                    publishFn = { result -> flushResults(result)
                    }
            )

            is SignInRequest.LinkData -> LinkDataWorker(
                    filesDir = filesDir,
                    activeAccount = ActiveAccount.loadFromStorage(keyValueStorage)!!,
                    authorizerId = params.authorizerId,
                    dataAddress = params.dataAddress,
                    key = params.key,
                    signalClient = SignalClient.Default(SignalStoreCriptext(db)),
                    db = db,
                    storage = keyValueStorage,
                    publishFn = {
                        result -> flushResults(result)
                    }
            )
            is SignInRequest.LinkDataReady -> LinkDataReadyWorker(
                    activeAccount = ActiveAccount.loadFromStorage(keyValueStorage)!!,
                    httpClient = httpClient,
                    publishFn = { result ->
                        flushResults(result)
                    }
            )
        }
    }
}
