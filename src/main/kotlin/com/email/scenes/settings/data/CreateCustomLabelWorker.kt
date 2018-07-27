package com.email.scenes.settings.data

import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.LabelTypes
import com.email.db.SettingsLocalDB
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.utils.ColorUtils
import com.email.utils.Utility

class CreateCustomLabelWorker(
        private val labelName: String,
        private val settingsLocalDB: SettingsLocalDB,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (SettingsResult.CreateCustomLabel) -> Unit)
    : BackgroundWorker<SettingsResult.CreateCustomLabel> {

    private val apiClient = SettingsAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.CreateCustomLabel {
        return SettingsResult.CreateCustomLabel.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.CreateCustomLabel>): SettingsResult.CreateCustomLabel? {
        val id = settingsLocalDB.labelDao.insert(Label(
                id = 0,
                text = labelName,
                color = ColorUtils.colorStringByName(labelName),
                visible = true,
                type = LabelTypes.CUSTOM
        ))
        if(id > 0) {
            val label = settingsLocalDB.labelDao.getLabelById(id)
            apiClient.postLabelCreatedEvent(label.text, label.color)
            return SettingsResult.CreateCustomLabel.Success(settingsLocalDB.labelDao.getLabelById(id))
        }
        return SettingsResult.CreateCustomLabel.Failure()
    }

    override fun cancel() {
    }

}