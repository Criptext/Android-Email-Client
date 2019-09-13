package com.criptext.mail.services.jobs

import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification.Companion.JOB_BACKUP_ID
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushData
import com.criptext.mail.push.notifiers.JobBackupNotifier
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupDataSource
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.scenes.settings.cloudbackup.workers.DataFileCreationWorker
import com.criptext.mail.scenes.settings.cloudbackup.workers.DeleteOldBackupWorker
import com.criptext.mail.scenes.settings.cloudbackup.workers.UploadBackupToDriveWorker
import com.criptext.mail.services.data.JobIdData
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.io.IOException
import java.util.*


class CloudBackupJobService: Job() {
    private var dataSource: CloudBackupDataSource? = null
    private var mDriveService: Drive? = null
    private val progressListener = JobServiceProgressListener()
    private var hasOldFile = false
    private var isBackupDone = false
    private var oldFileIds: List<String> = listOf()
    private var builder: NotificationCompat.Builder? = null

    private fun getAccountsSavedData(storage: KeyValueStorage): MutableList<JobIdData> {
        val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
        return if(savedJobsString.isEmpty()) mutableListOf()
        else JobIdData.fromJson(savedJobsString)
    }

    private fun handleNewPushNotification(activeAccount: ActiveAccount, storage: KeyValueStorage,
                                          upTickCounter: Boolean = true){
        val data = PushData.JobBackup(
                title = context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_switch)),
                body = context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_backing_up)),
                domain = activeAccount.domain,
                isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
                recipientId = activeAccount.recipientId,
                shouldPostNotification = true,
                progress = 0
        )
        val notificationId = (JOB_BACKUP_ID + activeAccount.id).toInt()
        val not = JobBackupNotifier.Open(data, notificationId)
        not.notifyPushEvent(context)
        if(upTickCounter) {
            val notCount = storage.getInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, 0)
            storage.putInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, notCount + 1)
        }
    }

    private fun updatePushNotification(activeAccount: ActiveAccount, text: String){
        val data = PushData.JobBackup(
                title = context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_switch)),
                body = text,
                domain = activeAccount.domain,
                isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
                recipientId = activeAccount.recipientId,
                shouldPostNotification = true,
                progress = 0
        )
        val notificationId = (JOB_BACKUP_ID + activeAccount.id).toInt()
        val not = JobBackupNotifier.Open(data, notificationId)
        not.notifyPushEvent(context)
    }

    private fun cancelPushNotification(activeAccount: ActiveAccount, storage: KeyValueStorage){
        val notificationId = (JOB_BACKUP_ID + activeAccount.id).toInt()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
        val notCount = storage.getInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, 0)
        if((notCount - 1) <= 0) {
            manager.cancel(JOB_BACKUP_ID)
        }
        storage.putInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, if(notCount <= 0) 0 else notCount - 1)
    }

    fun schedule(context: Context, intervalMillis: Long, accountId: Long, useWifiOnly: Boolean) {
        val builder = JobRequest.Builder(JOB_TAG)
        builder.setRequiredNetworkType(JobRequest.NetworkType.ANY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(intervalMillis, JobInfo.getMinFlexMillis())
        }else {
            builder.setPeriodic(intervalMillis)
        }
        val id = builder.build()
                .schedule()
        val storage = KeyValueStorage.SharedPrefs(context)
        val listOfJobs = getAccountsSavedData(storage)
        val accountSavedData = listOfJobs.find { it.accountId == accountId}
        if(accountSavedData != null) {
            listOfJobs.remove(accountSavedData)
        }
        listOfJobs.add(JobIdData(accountId, id, useWifiOnly))
        storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())

        isJobServiceOn(context, id)
    }

    fun cancel(context: Context, accountId: Long) {
        val storage = KeyValueStorage.SharedPrefs(context)
        val listOfJobs = getAccountsSavedData(storage)
        val accountSavedData = listOfJobs.find { it.accountId == accountId}
        if(accountSavedData != null) {
            listOfJobs.remove(accountSavedData)
            if(listOfJobs.isNotEmpty())
                storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())
            else
                storage.remove(listOf(KeyValueStorage.StringKey.SavedJobs))
            JobManager.instance().cancel(accountSavedData.jobId)
            Log.e("JOBSERVICE:", "Canceled!!!")
        }

    }

    private fun isJobServiceOn(context: Context, id: Int) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == id) {
                Log.e("JOBSERVICE:", "SCHEDULED!!!")
                break
            }
        }
    }

    override fun onRunJob(params: Params): Result {
        Log.e("JOBSERVICE:", "STARTED RUNNING!!!")
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isWiFi: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
        val storage = KeyValueStorage.SharedPrefs(context)
        val listOfJobs = getAccountsSavedData(storage)
        val accountSavedData = listOfJobs.find { it.jobId == params.id } ?: return Result.FAILURE
        val useWifiOnly = accountSavedData.useWifiOnly
        if(useWifiOnly && isConnected && isWiFi) {
            startWorking(accountSavedData.accountId)
        } else if(!useWifiOnly && isConnected && !isWiFi) {
            startWorking(accountSavedData.accountId)
        }
        return Result.SUCCESS
    }

    private fun newFileWorker(activeAccount: ActiveAccount, filesDir: File, db: AppDatabase): DataFileCreationWorker =
            DataFileCreationWorker(activeAccount = activeAccount, filesDir = filesDir, db = db, isFromJob = true,
                    passphrase = null, publishFn = {}, isLocal = false)

    private fun newUploadWorker(activeAccount: ActiveAccount, storage: KeyValueStorage, accountDao: AccountDao,
                                filePath: String, drive: Drive, driveProgress: MediaHttpUploaderProgressListener?): UploadBackupToDriveWorker =
            UploadBackupToDriveWorker(activeAccount = activeAccount, storage = storage, publishFn = {},
                    accountDao = accountDao, filePath = filePath, mDriveServiceHelper = drive, progressListener = driveProgress)

    private fun newDeleteWorker(fileIds: List<String>, drive: Drive): DeleteOldBackupWorker =
            DeleteOldBackupWorker(publishFn = {}, mDriveServiceHelper = drive, fileId = fileIds)

    private val reporter = object: ProgressReporter<CloudBackupResult> {
        override fun report(progressPercentage: CloudBackupResult) {

        }
    }


    private fun startWorking(accountId: Long){
        val db = AppDatabase.getAppDatabase(context)
        val account = db.accountDao().getAccountById(accountId)!!
        val activeAccount = ActiveAccount.loadFromDB(account)!!
        val storage = KeyValueStorage.SharedPrefs(context)

        handleNewPushNotification(activeAccount, storage)

        val filesDir = context.filesDir
        val fileWorker = newFileWorker(activeAccount, filesDir, db)

        val result = fileWorker.work(reporter)
        when(result){
            is CloudBackupResult.DataFileCreation.Success -> {
                updatePushNotification(
                        activeAccount = activeAccount,
                        text = context.getLocalizedUIMessage(UIMessage(R.string.uploading_to_google_drive))
                )
                mDriveService = getGoogleDriveService()
                if (mDriveService != null){
                    val uploadWorker = newUploadWorker(activeAccount, storage, db.accountDao(),
                            result.filePath, mDriveService!!, progressListener)
                    val uploadResult = uploadWorker.work(reporter)
                    when(uploadResult){
                        is CloudBackupResult.UploadBackupToDrive.Success -> {
                            hasOldFile = uploadResult.hasOldFile
                            oldFileIds = uploadResult.oldFileIds
                            while(!isBackupDone){}
                            updatePushNotification(
                                    activeAccount = activeAccount,
                                    text = context.getLocalizedUIMessage(UIMessage(R.string.upload_to_google_drive_success))
                            )
                            if(hasOldFile && isBackupDone) {
                                isBackupDone = false
                                val deleteWorker = newDeleteWorker(uploadResult.oldFileIds, mDriveService!!)
                                deleteWorker.work(reporter)
                                hasOldFile = false
                            }
                        }

                    }
                }
            }
        }
        cancelPushNotification(activeAccount, storage)
    }

    private fun getGoogleDriveService(): Drive? {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = googleAccount.account
        return Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential)
                .setApplicationName("Criptext Secure Email")
                .build()
    }

    inner class JobServiceProgressListener : MediaHttpUploaderProgressListener {
        @Throws(IOException::class)
        override fun progressChanged(uploader: MediaHttpUploader) {
            when (uploader.uploadState) {
                MediaHttpUploader.UploadState.INITIATION_STARTED -> {}
                MediaHttpUploader.UploadState.INITIATION_COMPLETE -> {}
                MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {}
                MediaHttpUploader.UploadState.MEDIA_COMPLETE -> {
                    if(hasOldFile && oldFileIds.isNotEmpty()) {
                        val deleteWorker = newDeleteWorker(oldFileIds, mDriveService!!)
                        deleteWorker.work(reporter)
                        hasOldFile = false
                        oldFileIds = listOf()
                    }
                    isBackupDone = true
                }
                else -> {}
            }
        }
    }

    companion object {
        const val JOB_TAG = "CRIPTEXT_CLOUD_BACKUP_JOB_SERVICE"
        const val PROGRESS_MAX = 100

        fun cancelJob(storage: KeyValueStorage, accountId: Long){
            val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
            val listOfJobs = if(savedJobsString.isEmpty()) mutableListOf()
            else JobIdData.fromJson(savedJobsString)
            val accountSavedData = listOfJobs.find { it.accountId == accountId}
            if(accountSavedData != null) {
                JobManager.instance().cancel(accountSavedData.jobId)
            }
        }
    }

}