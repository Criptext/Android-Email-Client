package com.criptext.mail.scenes.settings.profile.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.profile.workers.DeleteProfilePictureWorker
import java.io.File

class ProfileDataSource(
        private val cacheDir: File,
        var activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        private val storage: KeyValueStorage,
        private val accountDao: AccountDao,
        override val runner: WorkRunner)
    : BackgroundWorkManager<ProfileRequest, ProfileResult>(){

    override fun createWorkerFromParams(params: ProfileRequest,
                                        flushResults: (ProfileResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is ProfileRequest.DeleteProfilePicture -> DeleteProfilePictureWorker(
                    storage = storage,
                    accountDao = accountDao,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { res -> flushResults(res) }
            )
        }
    }
}