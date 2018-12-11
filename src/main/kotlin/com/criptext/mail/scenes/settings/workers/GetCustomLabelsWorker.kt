package com.criptext.mail.scenes.settings.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.data.SettingsResult

/**
 * Created by danieltigse on 28/06/18.
 */

class GetCustomLabelsWorker(
        private val db: SettingsLocalDB,
        override val publishFn: (
                SettingsResult.GetCustomLabels) -> Unit)
    : BackgroundWorker<SettingsResult.GetCustomLabels> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SettingsResult.GetCustomLabels {
        return SettingsResult.GetCustomLabels.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.GetCustomLabels>): SettingsResult.GetCustomLabels? {
        val labels = db.labelDao.getAllCustomLabels().toMutableList()
        //We threat starred label as a custom label
        labels.add(0, Label.defaultItems.starred)
        return SettingsResult.GetCustomLabels.Success(labels)
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
