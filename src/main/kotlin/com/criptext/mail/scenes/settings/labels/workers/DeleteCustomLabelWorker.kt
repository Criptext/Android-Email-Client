package com.criptext.mail.scenes.settings.labels.workers

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.labels.data.LabelsResult
import com.criptext.mail.utils.ColorUtils
import com.criptext.mail.utils.peerdata.PeerCreateLabelData
import com.criptext.mail.utils.peerdata.PeerDeleteLabelData
import com.github.kittinunf.result.Result
import java.util.*

class DeleteCustomLabelWorker(
        private val labelUUID: String,
        private val settingsLocalDB: SettingsLocalDB,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        override val publishFn: (LabelsResult.DeleteCustomLabel) -> Unit)
    : BackgroundWorker<LabelsResult.DeleteCustomLabel> {

    private val peerApiClient = PeerEventsApiHandler.Default(httpClient,
            activeAccount, settingsLocalDB.pendingEventDao, storage, settingsLocalDB.accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): LabelsResult.DeleteCustomLabel {
        return LabelsResult.DeleteCustomLabel.Failure()
    }

    override fun work(reporter: ProgressReporter<LabelsResult.DeleteCustomLabel>): LabelsResult.DeleteCustomLabel? {
        val operation = Result.of { settingsLocalDB.labelDao.deleteByLabelUUID(labelUUID, activeAccount.id) }

        return when(operation){
            is Result.Success -> {
                peerApiClient.enqueueEvent(PeerDeleteLabelData(labelUUID).toJSON())
                LabelsResult.DeleteCustomLabel.Success(labelUUID)
            }
            is Result.Failure -> LabelsResult.DeleteCustomLabel.Failure()
        }
    }

    override fun cancel() {
    }

}