package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result


class SetCloudBackupActiveWorker(val activeAccount: ActiveAccount,
                                 private val cloudBackupData: CloudBackupData,
                                 private val accountDao: AccountDao,
                                 override val publishFn: (GeneralResult) -> Unit)
    : BackgroundWorker<GeneralResult.SetCloudBackupActive> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): GeneralResult.SetCloudBackupActive {
        return if(ex is ServerErrorException) {
            GeneralResult.SetCloudBackupActive.Failure(UIMessage(R.string.server_bad_status), ex, cloudBackupData)
        }else {
            GeneralResult.SetCloudBackupActive.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())), ex, cloudBackupData)
        }
    }

    override fun work(reporter: ProgressReporter<GeneralResult.SetCloudBackupActive>): GeneralResult.SetCloudBackupActive? {
        val result =  Result.of {
            accountDao.setGoogleDriveActive(activeAccount.id,
                    wifiOnly = cloudBackupData.useWifiOnly,
                    backupFrequency = cloudBackupData.autoBackupFrequency,
                    googleDriveIsActive = cloudBackupData.hasCloudBackup)
        }

        return when (result) {
            is Result.Success -> GeneralResult.SetCloudBackupActive.Success(
                    cloudBackupData = cloudBackupData
            )

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}