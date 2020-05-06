package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.settings.data.SettingsAPIClient
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.peerdata.PeerThreadReadData
import com.github.kittinunf.result.Result

class ChangeBlockRemoteContentSettingWorker(
        private val accountDao: AccountDao,
        private val newBlockRemoteContentSetting: Boolean,
        httpClient: HttpClient,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        override val publishFn: (GeneralResult.ChangeBlockRemoteContentSetting) -> Unit)
    : BackgroundWorker<GeneralResult.ChangeBlockRemoteContentSetting> {

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.ChangeBlockRemoteContentSetting =
            GeneralResult.ChangeBlockRemoteContentSetting.Failure(newBlockRemoteContentSetting, createErrorMessage(ex))


    override fun work(reporter: ProgressReporter<GeneralResult.ChangeBlockRemoteContentSetting>): GeneralResult.ChangeBlockRemoteContentSetting? {
        val result =  Result.of {
            apiClient.putChangeBlockRemoteContent(newBlockRemoteContentSetting)
            accountDao.updateBlockRemoteContent(newBlockRemoteContentSetting, activeAccount.recipientId, activeAccount.domain)
            activeAccount.updateAccountBlockedRemoteContent(storage, newBlockRemoteContentSetting)
        }
        return when (result) {
            is Result.Success -> {
                GeneralResult.ChangeBlockRemoteContentSetting.Success(newBlockRemoteContentSetting)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.error_updating_status)
        }
    }
}

