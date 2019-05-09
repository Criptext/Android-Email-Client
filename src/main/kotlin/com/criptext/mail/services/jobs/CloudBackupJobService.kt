package com.criptext.mail.services.jobs

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupDataSource
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupRequest
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.services.data.JobIdData
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.IOException
import java.util.*


class CloudBackupJobService: Job() {
    private var dataSource: CloudBackupDataSource? = null
    private var mDriveService: Drive? = null
    private val progressListener = JobServiceProgressListener()
    private var hasOldFile = false
    private var isBackupDone = false
    private var oldFileIds: List<String> = listOf()

    private val dataSourceListener: (CloudBackupResult) -> Unit = { result ->
        when(result) {
            is CloudBackupResult.UploadBackupToDrive -> onUploadBackupToDrive(result)
            is CloudBackupResult.DataFileCreation -> onDataFileCreated(result)
        }
    }

    fun schedule(context: Context, intervalMillis: Long, accountId: Long) {
        val builder = JobRequest.Builder(CloudBackupJobService.JOB_TAG)
        builder.setRequiredNetworkType(JobRequest.NetworkType.ANY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(intervalMillis, JobInfo.getMinFlexMillis())
        }else {
            builder.setPeriodic(intervalMillis)
        }
        val id = builder.build()
                .schedule()
        val storage = KeyValueStorage.SharedPrefs(context)
        val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
        val listOfJobs = if(savedJobsString.isEmpty()) mutableListOf()
        else JobIdData.fromJson(savedJobsString)
        val accountSavedData = listOfJobs.find { it.accountId == accountId}
        if(accountSavedData != null) {
            listOfJobs.remove(accountSavedData)
        }
        listOfJobs.add(JobIdData(accountId, id))
        storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())

        isJobServiceOn(context, id)
    }

    fun cancel(context: Context, accountId: Long) {
        val storage = KeyValueStorage.SharedPrefs(context)
        val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
        val listOfJobs = if(savedJobsString.isEmpty()) mutableListOf()
        else JobIdData.fromJson(savedJobsString)
        val accountSavedData = listOfJobs.find { it.accountId == accountId}
        if(accountSavedData != null) {
            JobManager.instance().cancel(accountSavedData.jobId)
            Log.e("JOBSERVICE:", "Canceled!!!")
        }

    }

    private fun isJobServiceOn(context: Context, id: Int) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == id) {
                Log.e("JOBSERVICE:", "Is Running!!!")
                break
            }
        }
    }

    override fun onRunJob(params: Params): Result {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI
        val storage = KeyValueStorage.SharedPrefs(context)
        val useWifiOnly = storage.getBool(KeyValueStorage.StringKey.UseWifiOnlyForBackup, true)
        if(useWifiOnly && isConnected && isWiFi) {
            startWorking()
        } else if(!useWifiOnly && isConnected && !isWiFi) {
            startWorking()
        }
        return Result.SUCCESS
    }


    private fun startWorking(){
        dataSource = CloudBackupDataSource(
                activeAccount = ActiveAccount.loadFromStorage(context)!!,
                filesDir = context.filesDir,
                db = AppDatabase.getAppDatabase(context),
                storage = KeyValueStorage.SharedPrefs(context),
                runner = AsyncTaskWorkRunner()
        )
        dataSource?.listener = dataSourceListener
        dataSource?.submitRequest(CloudBackupRequest.DataFileCreation(null))
    }

    private fun onDataFileCreated(result: CloudBackupResult.DataFileCreation){
        when(result){
            is CloudBackupResult.DataFileCreation.Success -> {
                mDriveService = getGoogleDriveService()
                if (mDriveService != null){
                    dataSource?.submitRequest(CloudBackupRequest.UploadBackupToDrive(result.filePath, mDriveService!!,
                            progressListener))
                }
            }
        }
    }

    private fun onUploadBackupToDrive(result: CloudBackupResult.UploadBackupToDrive){
        when(result){
            is CloudBackupResult.UploadBackupToDrive.Success -> {
                hasOldFile = result.hasOldFile
                oldFileIds = result.oldFileIds
                if(hasOldFile && isBackupDone) {
                    isBackupDone = false
                    dataSource?.submitRequest(CloudBackupRequest.DeleteFileInDrive(mDriveService!!, oldFileIds!!))
                    hasOldFile = false
                }
                Log.e("Cloud Backup",
                        "Cloud Backup Uploaded!!")
            }
        }
    }

    private fun getGoogleDriveService(): Drive? {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = googleAccount.account
        return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
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
                    isBackupDone = true
                    if(hasOldFile && oldFileIds.isNotEmpty()) {
                        dataSource?.submitRequest(CloudBackupRequest.DeleteFileInDrive(mDriveService!!, oldFileIds))
                        hasOldFile = false
                        oldFileIds = listOf()
                    }
                }
            }
        }
    }

    companion object {
        const val JOB_TAG = "CRIPTEXT_CLOUD_BACKUP_JOB_SERVICE"
    }

}