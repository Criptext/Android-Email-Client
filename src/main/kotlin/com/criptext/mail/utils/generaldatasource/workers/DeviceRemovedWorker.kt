package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.EmailUtils
import com.criptext.mail.utils.generaldatasource.RemoveDeviceUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import java.io.File

class DeviceRemovedWorker(private val letAPIKnow: Boolean,
                          private val filesDir: File,
                          private val activeAccount: ActiveAccount,
                          private val httpClient: HttpClient,
                          private val db: AppDatabase,
                          private val storage: KeyValueStorage,
                          override val publishFn: (GeneralResult.DeviceRemoved) -> Unit
                          ) : BackgroundWorker<GeneralResult.DeviceRemoved> {

    override val canBeParallelized = false
    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)
    private var newActiveAccount: ActiveAccount? = null

    override fun catchException(ex: Exception): GeneralResult.DeviceRemoved {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.DeviceRemoved>)
            : GeneralResult.DeviceRemoved? {

        val deleteOperation = workOperation()


        val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(deleteOperation)

        val finalResult = if(sessionExpired)
            newRetryWithNewSessionOperation()
        else
            deleteOperation

        val accounts = db.accountDao().getLoggedInAccounts()
        if(accounts.isNotEmpty()){
            db.accountDao().updateActiveInAccount()
            db.accountDao().updateActiveInAccount(accounts.first().id)
            newActiveAccount = AccountUtils.setUserAsActiveAccount(accounts.first(), storage)
        }

        return when (finalResult){
            is Result.Success -> {
                GeneralResult.DeviceRemoved.Success(newActiveAccount)
            }
            is Result.Failure -> {
                GeneralResult.DeviceRemoved.Failure()
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }

    private fun workOperation() : Result<Unit, Exception> = if(letAPIKnow) {
            Result.of { apiClient.postLogout()
        }
        .flatMap { Result.of {
            EmailUtils.deleteEmailsInFileSystem(filesDir, activeAccount.recipientId)
            RemoveDeviceUtils.clearAllData(db, storage, activeAccount.recipientId)
        }}}
    else
        Result.of {
            EmailUtils.deleteEmailsInFileSystem(filesDir, activeAccount.recipientId)
            RemoveDeviceUtils.clearAllData(db, storage, activeAccount.recipientId)
        }

    private fun newRetryWithNewSessionOperation()
            : Result<Unit, Exception> {
        val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient,
                activeAccount, storage, db.accountDao())
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        return when(refreshOperation){
            is Result.Success -> {
                val account = ActiveAccount.loadFromStorage(storage)!!
                apiClient.token = account.jwt
                workOperation()
            }
            is Result.Failure -> {
                Result.of { throw refreshOperation.error }
            }
        }
    }
}