package com.email.scenes.settings.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.LabelTypes
import com.email.db.SettingsLocalDB
import com.email.db.models.Label
import com.email.utils.ColorUtils
import com.email.utils.Utility

class CreateCustomLabelWorker(
        private val labelName: String,
        private val settingsLocalDB: SettingsLocalDB,
        override val publishFn: (SettingsResult.CreateCustomLabel) -> Unit)
    : BackgroundWorker<SettingsResult.CreateCustomLabel> {

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
        if(id > 0)
            return SettingsResult.CreateCustomLabel.Success(settingsLocalDB.labelDao.getLabelById(id))
        return SettingsResult.CreateCustomLabel.Failure()
    }

    override fun cancel() {
    }

}