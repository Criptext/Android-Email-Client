package com.criptext.mail.scenes.settings.labels.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.labels.data.LabelsResult

/**
 * Created by danieltigse on 28/06/18.
 */

class GetCustomLabelsWorker(
        private val db: SettingsLocalDB,
        private val activeAccount: ActiveAccount,
        override val publishFn: (
                LabelsResult.GetCustomLabels) -> Unit)
    : BackgroundWorker<LabelsResult.GetCustomLabels> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): LabelsResult.GetCustomLabels {
        return LabelsResult.GetCustomLabels.Failure()
    }

    override fun work(reporter: ProgressReporter<LabelsResult.GetCustomLabels>): LabelsResult.GetCustomLabels? {
        val labels = db.labelDao.getAllCustomLabels(activeAccount.id).toMutableList()
        //We threat starred label as a custom label
        labels.add(0, Label.defaultItems.starred)
        return LabelsResult.GetCustomLabels.Success(labels)
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
