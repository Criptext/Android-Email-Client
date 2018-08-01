package com.criptext.mail.scenes.settings.data

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.Label

/**
 * Created by danieltigse on 10/07/18.
 */

class ChangeVisibilityLabelWorker(
        private val db: SettingsLocalDB,
        private val labelId: Long,
        private val isVisible: Boolean,
        override val publishFn: (
                SettingsResult.ChangeVisibilityLabel) -> Unit)
    : BackgroundWorker<SettingsResult.ChangeVisibilityLabel> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): SettingsResult.ChangeVisibilityLabel {
        return SettingsResult.ChangeVisibilityLabel.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ChangeVisibilityLabel>): SettingsResult.ChangeVisibilityLabel? {
        db.labelDao.updateVisibility(labelId, isVisible)
        return SettingsResult.ChangeVisibilityLabel.Success()
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

}
