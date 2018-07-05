package com.email.scenes.settings.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.SettingsLocalDB
import com.email.db.models.Contact

class ChangeContactNameWorker(
        private val fullName: String,
        private val recipientId: String,
        private val settingsLocalDB: SettingsLocalDB,
        override val publishFn: (SettingsResult.ChangeContactName) -> Unit)
    : BackgroundWorker<SettingsResult.ChangeContactName> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SettingsResult.ChangeContactName {
        return SettingsResult.ChangeContactName.Failure()
    }

    override fun work(reporter: ProgressReporter<SettingsResult.ChangeContactName>): SettingsResult.ChangeContactName? {
        settingsLocalDB.contactDao.updateContactName("$recipientId@${Contact.mainDomain}", fullName)
        settingsLocalDB.accountDao.updateProfileName(fullName, recipientId)
        return SettingsResult.ChangeContactName.Success(fullName)
    }

    override fun cancel() {
    }

}