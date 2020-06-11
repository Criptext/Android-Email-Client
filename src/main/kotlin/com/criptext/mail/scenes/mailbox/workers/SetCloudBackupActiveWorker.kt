package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result


class SetCloudBackupActiveWorker(val activeAccount: ActiveAccount,
                                 private val cloudBackupData: CloudBackupData,
                                 private val accountDao: AccountDao,
                                 override val publishFn: (MailboxResult) -> Unit)
    : BackgroundWorker<MailboxResult.SetCloudBackupActive> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.SetCloudBackupActive {
        return if(ex is ServerErrorException) {
            MailboxResult.SetCloudBackupActive.Failure(UIMessage(R.string.server_bad_status), ex, cloudBackupData)
        }else {
            MailboxResult.SetCloudBackupActive.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())), ex, cloudBackupData)
        }
    }

    override fun work(reporter: ProgressReporter<MailboxResult.SetCloudBackupActive>): MailboxResult.SetCloudBackupActive? {
        val result =  Result.of {
            accountDao.setGoogleDriveActive(activeAccount.id,
                    wifiOnly = cloudBackupData.useWifiOnly,
                    backupFrequency = cloudBackupData.autoBackupFrequency,
                    googleDriveIsActive = cloudBackupData.hasCloudBackup)
        }

        return when (result) {
            is Result.Success -> MailboxResult.SetCloudBackupActive.Success(
                    cloudBackupData = cloudBackupData
            )

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}