package com.email.scenes.settings.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.SettingsLocalDB
import com.email.db.models.Label

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
