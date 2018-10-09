package com.criptext.mail.scenes.linking.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount

class LinkingDataSource(
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private val httpClient: HttpClient,
        override val runner: WorkRunner)
    : BackgroundWorkManager<LinkingRequest, LinkingResult>(){

    override fun createWorkerFromParams(params: LinkingRequest,
                                        flushResults: (LinkingResult) -> Unit): BackgroundWorker<*> {

        return when(params){
            is LinkingRequest.CheckForKeyBundle -> CheckForKeyBundleWorker(
                    deviceId = params.deviceId,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = { result ->
                        flushResults(result) }
            )
        }
    }
}