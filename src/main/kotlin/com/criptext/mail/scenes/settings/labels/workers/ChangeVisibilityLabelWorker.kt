package com.criptext.mail.scenes.settings.labels.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.labels.data.LabelsResult

/**
 * Created by danieltigse on 10/07/18.
 */

class ChangeVisibilityLabelWorker(
        private val activeAccount: ActiveAccount,
        private val db: SettingsLocalDB,
        private val labelId: Long,
        private val isVisible: Boolean,
        override val publishFn: (
                LabelsResult.ChangeVisibilityLabel) -> Unit)
    : BackgroundWorker<LabelsResult.ChangeVisibilityLabel> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): LabelsResult.ChangeVisibilityLabel {
        return LabelsResult.ChangeVisibilityLabel.Failure()
    }

    override fun work(reporter: ProgressReporter<LabelsResult.ChangeVisibilityLabel>): LabelsResult.ChangeVisibilityLabel? {
        db.labelDao.updateVisibility(labelId, isVisible, activeAccount.id)
        return LabelsResult.ChangeVisibilityLabel.Success()
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
