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
import com.github.kittinunf.result.Result
import java.util.*

class CreateCustomLabelWorker(
        private val labelName: String,
        private val settingsLocalDB: SettingsLocalDB,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        override val publishFn: (LabelsResult.CreateCustomLabel) -> Unit)
    : BackgroundWorker<LabelsResult.CreateCustomLabel> {

    private val peerApiClient = PeerEventsApiHandler.Default(activeAccount, settingsLocalDB.pendingEventDao,
            storage, settingsLocalDB.accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): LabelsResult.CreateCustomLabel {
        return LabelsResult.CreateCustomLabel.Failure()
    }

    override fun work(reporter: ProgressReporter<LabelsResult.CreateCustomLabel>): LabelsResult.CreateCustomLabel? {
        val label = Label(
                id = 0,
                text = labelName,
                color = ColorUtils.colorStringByName(labelName),
                visible = true,
                type = LabelTypes.CUSTOM,
                uuid = UUID.randomUUID().toString(),
                accountId = activeAccount.id)
        val operation = Result.of { settingsLocalDB.labelDao.insert(label) }

        return when(operation){
            is Result.Success -> {
                peerApiClient.enqueueEvent(PeerCreateLabelData(label.text, label.color, label.uuid).toJSON())
                LabelsResult.CreateCustomLabel.Success(settingsLocalDB.labelDao.getLabelById(operation.value, activeAccount.id))
            }
            is Result.Failure -> LabelsResult.CreateCustomLabel.Failure()
        }
    }

    override fun cancel() {
    }

}