package com.criptext.mail.scenes.settings.cloudbackup.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result


class SetCloudBackupActiveWorker(val activeAccount: ActiveAccount,
                                 private val cloudBackupData: CloudBackupData,
                                 private val accountDao: AccountDao,
                                 override val publishFn: (CloudBackupResult) -> Unit)
    : BackgroundWorker<CloudBackupResult.SetCloudBackupActive> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): CloudBackupResult.SetCloudBackupActive {
        return if(ex is ServerErrorException) {
            CloudBackupResult.SetCloudBackupActive.Failure(UIMessage(R.string.server_bad_status), ex, cloudBackupData)
        }else {
            CloudBackupResult.SetCloudBackupActive.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())), ex, cloudBackupData)
        }
    }

    override fun work(reporter: ProgressReporter<CloudBackupResult.SetCloudBackupActive>): CloudBackupResult.SetCloudBackupActive? {
        val result =  Result.of {
            accountDao.setGoogleDriveActive(activeAccount.id,
                    wifiOnly = cloudBackupData.useWifiOnly,
                    backupFrequency = cloudBackupData.autoBackupFrequency,
                    googleDriveIsActive = cloudBackupData.hasCloudBackup)
        }

        return when (result) {
            is Result.Success -> CloudBackupResult.SetCloudBackupActive.Success(
                    cloudBackupData = cloudBackupData
            )

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}