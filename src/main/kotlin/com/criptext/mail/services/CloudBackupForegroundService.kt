package com.criptext.mail.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.crashlytics.android.Crashlytics
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.CriptextNotification.Companion.CHANNEL_ID_JOB_BACKUP
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushController
import com.criptext.mail.push.PushData
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.notifiers.JobBackupNotifier
import com.criptext.mail.push.notifiers.Notifier
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupResult
import com.criptext.mail.scenes.settings.cloudbackup.workers.DataFileCreationWorker
import com.criptext.mail.scenes.settings.cloudbackup.workers.DeleteOldBackupWorker
import com.criptext.mail.scenes.settings.cloudbackup.workers.UploadBackupToDriveWorker
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import io.fabric.sdk.android.Fabric
import java.io.File
import java.io.IOException
import java.util.*

class CloudBackupForegroundService: Service() {

    private var notificationManager: NotificationManager? = null
    private var accountId: Long? = null
    private var accountEmail: String? = null
    private var mDriveService: Drive? = null
    private val progressListener = JobServiceProgressListener()
    private var hasOldFile = false
    private var isBackupDone = false
    private var oldFileIds: List<String> = listOf()


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        accountId = intent?.extras?.getLong("accountId")
        accountEmail = intent?.extras?.getString("accountEmail")
        if(accountId == null){
            stopService()
        } else {
            startForegroundService()
            startWorking(accountId!!)
        }
        return START_STICKY
    }

    private fun startForegroundService() {

        //Create Notification channel for all the notifications sent from this app.
        createNotificationChannel()

        val storage = KeyValueStorage.SharedPrefs(this)

        // Start foreground service.
        prepareNotification(storage)

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
        val db = AppDatabase.getAppDatabase(this)
        val storage = KeyValueStorage.SharedPrefs(this)
        val account = db.accountDao().getAccountById(accountId) ?: return stopService()
        val activeAccount = ActiveAccount.loadFromDB(account)!!

        val filesDir = this.filesDir
        val fileWorker = newFileWorker(activeAccount, filesDir, db)

        val result = fileWorker.work(reporter)
        when(result){
            is CloudBackupResult.DataFileCreation.Success -> {
                updatePushNotification(
                        activeAccount = activeAccount,
                        text = this.getLocalizedUIMessage(UIMessage(R.string.uploading_to_google_drive))
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
                                    text = this.getLocalizedUIMessage(UIMessage(R.string.upload_to_google_drive_success))
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
        stopService()
    }

    private fun stopService() {
        // Stop foreground service and remove the notification.
        stopForeground(true)
        // Stop the foreground service.
        stopSelf()

        IS_RUNNING = false
    }


    override fun onDestroy() {
        IS_RUNNING = false
    }

    private fun prepareNotification(storage: KeyValueStorage){
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID_JOB_BACKUP)
                .setSmallIcon(R.drawable.push_icon)
                .setContentTitle(this.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_switch)))
                .setContentText(this.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_backing_up)))
                .setSubText(accountEmail)
                .setOngoing(true)
                .setGroup(CriptextNotification.ACTION_JOB_BACKUP)
                .setGroupSummary(false)
                .setStyle(NotificationCompat.BigTextStyle().bigText(this.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_backing_up))))
                .setProgress(0,0,true)
                .build()
        val notificationId = (CriptextNotification.JOB_BACKUP_ID + accountId!!).toInt()
        startForeground(notificationId, notification)
        val notCount = storage.getInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, 0)
        storage.putInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, notCount + 1)
        IS_RUNNING = true
    }

    private fun updatePushNotification(activeAccount: ActiveAccount, text: String){
        val data = PushData.JobBackup(
                title = this.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_switch)),
                body = text,
                domain = activeAccount.domain,
                isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
                recipientId = activeAccount.recipientId,
                shouldPostNotification = true,
                progress = 0
        )
        val notificationId = (CriptextNotification.JOB_BACKUP_ID + activeAccount.id).toInt()
        val not = JobBackupNotifier.Open(data, notificationId)
        not.notifyPushEvent(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                    notificationManager?.getNotificationChannel(CHANNEL_ID_JOB_BACKUP)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                        CHANNEL_ID_JOB_BACKUP,
                        this.getLocalizedUIMessage(UIMessage(R.string.job_backup_notification)),
                        NotificationManager.IMPORTANCE_LOW
                )
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    private fun cancelPushNotification(activeAccount: ActiveAccount, storage: KeyValueStorage){
        val notCount = storage.getInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, 0)
        storage.putInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, if(notCount <= 0) 0 else notCount - 1)
    }

    fun notifyPushEvent(notifier: Notifier?){
        notifier?.notifyPushEvent(applicationContext)
    }

    private fun getGoogleDriveService(): Drive? {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE_FILE))
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
        var IS_RUNNING: Boolean = false
    }

}