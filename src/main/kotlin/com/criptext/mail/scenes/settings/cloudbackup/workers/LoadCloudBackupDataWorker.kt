package com.criptext.mail.scenes.settings.cloudbackup.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.google.api.services.drive.Drive
import java.util.*


class LoadCloudBackupDataWorker(val activeAccount: ActiveAccount,
                                val mDriveServiceHelper: Drive?,
                                private val storage: KeyValueStorage,
                                private val accountDao: AccountDao,
                                override val publishFn: (CloudBackupResult) -> Unit)
    : BackgroundWorker<CloudBackupResult.LoadCloudBakcupData> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): CloudBackupResult.LoadCloudBakcupData {
        return if(ex is ServerErrorException) {
            when(ex.errorCode) {
                ServerCodes.MethodNotAllowed -> CloudBackupResult.LoadCloudBakcupData.Failure(UIMessage(R.string.message_warning_two_fa), ex)
                else -> CloudBackupResult.LoadCloudBakcupData.Failure(UIMessage(R.string.server_error_exception), ex)
            }
        }else {
            CloudBackupResult.LoadCloudBakcupData.Failure(UIMessage(R.string.server_error_exception), ex)
        }
    }

    override fun work(reporter: ProgressReporter<CloudBackupResult.LoadCloudBakcupData>): CloudBackupResult.LoadCloudBakcupData? {
        val result =  Result.of {
            val account = accountDao.getAccountById(activeAccount.id) ?: throw Exception()
            val info = if(mDriveServiceHelper != null){
                val parentFolder = mDriveServiceHelper.files().list().setQ("name='Criptext Backups'").execute()
                if(parentFolder.files.isEmpty())
                    throw Exception()
                val folder = mDriveServiceHelper.files().list().setQ("name='${activeAccount.userEmail}' and ('${parentFolder.files.first().id}' in parents)")
                        .execute()
                if(folder.files.isEmpty()){
                    throw Exception()
                } else {
                    val file = mDriveServiceHelper.files().list()
                            .setQ("name contains 'Mailbox Backup' and ('${folder.files.first().id}' in parents) and trashed=false")
                            .setFields("*")
                            .execute()
                    if(file.files.isEmpty())
                        throw Exception()
                    else {
                        val driveFile = file.files.first()
                        Pair(driveFile.getSize() / (1024 * 1024), driveFile.modifiedTime.value)
                    }
                }
            } else Pair(storage.getLong(KeyValueStorage.StringKey.LastBackupSize, 0), storage.getLong(KeyValueStorage.StringKey.LastBackupDate, 0))
            Triple(account, info.first, info.second)
        }

        return when (result) {
            is Result.Success -> CloudBackupResult.LoadCloudBakcupData.Success(
                    cloudBackupData = CloudBackupData(
                        hasCloudBackup = result.value.first.hasCloudBackup,
                        useWifiOnly = result.value.first.wifiOnly,
                        autoBackupFrequency = result.value.first.autoBackupFrequency,
                        lastModified = Date(result.value.third),
                        fileSize = result.value.second.toInt()
                    )
            )

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}