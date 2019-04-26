package com.criptext.mail.services

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


class CloudBackupJobService: JobService() {

    private val JOB_ID = 2120
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
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, CloudBackupJobService::class.java)
        val jobId = JOB_ID.toString().plus(accountId)
        val builder = JobInfo.Builder(jobId.toInt(), componentName)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(intervalMillis, JobInfo.getMinFlexMillis())
        }else {
            builder.setPeriodic(intervalMillis)
        }
        builder.setPersisted(true)
        jobScheduler.schedule(builder.build())
        isJobServiceOn(context)
    }

    fun cancel(context: Context, accountId: Long) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobId = JOB_ID.toString().plus(accountId)
        jobScheduler.cancel(jobId.toInt())
        Log.e("JOBSERVICE:", "Canceled!!!")
    }

    private fun isJobServiceOn(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                Log.e("JOBSERVICE:", "Is Running!!!")
                break
            }
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI
        val storage = KeyValueStorage.SharedPrefs(this)
        val useWifiOnly = storage.getBool(KeyValueStorage.StringKey.UseWifiOnlyForBackup, true)
        if(useWifiOnly && isConnected && isWiFi) {
            startWorking()
        } else if(!useWifiOnly && isConnected && !isWiFi) {
            startWorking()
        }
        return false
    }

    private fun startWorking(){
        dataSource = CloudBackupDataSource(
                activeAccount = ActiveAccount.loadFromStorage(applicationContext)!!,
                filesDir = applicationContext.filesDir,
                db = AppDatabase.getAppDatabase(applicationContext),
                storage = KeyValueStorage.SharedPrefs(this),
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
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE_FILE))
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

}