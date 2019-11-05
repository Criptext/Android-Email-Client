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
import com.criptext.mail.utils.peerdata.PeerEditLabelData
import com.github.kittinunf.result.Result
import java.util.*

class EditCustomLabelWorker(
        private val labelUUID: String,
        private val newName: String,
        private val settingsLocalDB: SettingsLocalDB,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        override val publishFn: (LabelsResult.EditCustomLabel) -> Unit)
    : BackgroundWorker<LabelsResult.EditCustomLabel> {

    private val peerApiClient = PeerEventsApiHandler.Default(httpClient,
            activeAccount, settingsLocalDB.pendingEventDao, storage, settingsLocalDB.accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): LabelsResult.EditCustomLabel {
        return LabelsResult.EditCustomLabel.Failure()
    }

    override fun work(reporter: ProgressReporter<LabelsResult.EditCustomLabel>): LabelsResult.EditCustomLabel? {
        val operation = Result.of {
            settingsLocalDB.labelDao.updateLabelName(newName, labelUUID, activeAccount.id)
        }

        return when(operation){
            is Result.Success -> {
                val label = settingsLocalDB.labelDao.getByUUID(labelUUID, activeAccount.id)
                if(label != null)
                    peerApiClient.enqueueEvent(PeerEditLabelData(labelUUID, newName, label.color).toJSON())
                LabelsResult.EditCustomLabel.Success(labelUUID, newName)
            }
            is Result.Failure -> LabelsResult.EditCustomLabel .Failure()
        }
    }

    override fun cancel() {
    }

}