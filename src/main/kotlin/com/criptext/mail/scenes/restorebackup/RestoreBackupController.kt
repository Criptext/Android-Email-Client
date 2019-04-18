package com.criptext.mail.scenes.restorebackup

import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.RestoreBackupParams
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupRequest
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.google.api.client.googleapis.media.MediaHttpDownloader
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener
import com.google.api.services.drive.Drive

class RestoreBackupController(
        private val model: RestoreBackupModel,
        private val scene: RestoreBackupScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val websocketEvents: WebSocketEventPublisher,
        private val dataSource: BackgroundWorkManager<RestoreBackupRequest, RestoreBackupResult>)
    : SceneController(){



    override val menuResourceId: Int? = null


    private val dataSourceListener: (RestoreBackupResult) -> Unit = { result ->
        when(result) {
            is RestoreBackupResult.CheckForBackup -> onCheckForBackup(result)
            is RestoreBackupResult.DownloadBackup -> onDownloadBackup(result)
            is RestoreBackupResult.RestoreMailbox -> onRestoreMailbox(result)
        }
    }

    private val progressListener = RestoreProgressListener()

    val googleSignInListener = object: GoogleSignInObserver {
        override fun signInSuccess(drive: Drive){
            model.mDriveServiceHelper = drive
            dataSource.submitRequest(RestoreBackupRequest.CheckForBackup(model.mDriveServiceHelper!!))
        }

        override fun signInFailed(){
            scene.showMessage(UIMessage(R.string.login_fail_try_again_error_exception))
        }
    }

    private val uiObserver = object: RestoreBackupUIObserver{
        override fun onPasswordChangedListener(password: String) {
            if(password.isNotEmpty() && password.length >= 3) {
                model.passphrase = password
                scene.enableRestoreButton(true)
            } else {
                model.passphrase = null
                scene.enableRestoreButton(false)
            }
        }

        override fun onRetryRestore() {
            scene.setProgress(0)
            scene.showBackupFoundLayout(model.isFileEncrypted)
        }

        override fun onChangeDriveAccount() {
            scene.enableRestoreButton(false)
            host.launchExternalActivityForResult(ExternalActivityParams.ChangeAccountGoogleDrive())
        }

        override fun onRestore() {
            scene.showProgressLayout()
            scene.setProgress(20)
            if(model.backupFilePath.isNotEmpty()){
                dataSource.submitRequest(RestoreBackupRequest.RestoreMailbox(model.backupFilePath, model.passphrase))
            }else{
                dataSource.submitRequest(RestoreBackupRequest.DownloadBackup(model.mDriveServiceHelper!!, progressListener))
            }
        }

        override fun onCancelRestore() {
            host.exitToScene(MailboxParams(), null, false, true)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        websocketEvents.setListener(webSocketEventListener)
        model.mDriveServiceHelper = scene.getGoogleDriveService()
        if(model.mDriveServiceHelper == null)
            host.exitToScene(MailboxParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.restore_backup_no_account)), true)

        dataSource.submitRequest(RestoreBackupRequest.CheckForBackup(model.mDriveServiceHelper!!))
        model.accountEmail = activeAccount.userEmail

        scene.attachView(model = model, uiObserver = uiObserver)
        dataSource.listener = dataSourceListener
        return handleActivityMessage(activityMessage)
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return handleActivityMessage(activityMessage)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {

        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {

        }

        override fun onSyncRequestDeny() {

        }

        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onNewEvent() {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {

        }

        override fun onDeviceRemoved() {

        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }

    private fun onCheckForBackup(result: RestoreBackupResult.CheckForBackup){
        when(result){
            is RestoreBackupResult.CheckForBackup.Success -> {
                model.lastModified = result.lastModified
                model.backupSize = result.fileSize.toInt()
                model.isFileEncrypted = result.isEncrypted
                scene.enableRestoreButton(!result.isEncrypted)
                scene.showBackupFoundLayout(model.isFileEncrypted)
                scene.updateFileData(model.backupSize, model.lastModified)
            }
            is RestoreBackupResult.CheckForBackup.Failure -> {
                scene.showBackupNotFoundLayout()
            }
        }
    }

    private fun onDownloadBackup(result: RestoreBackupResult.DownloadBackup){
        when(result){
            is RestoreBackupResult.DownloadBackup.Success -> {
                model.backupFilePath = result.filePath
                if(!model.hasPathReady){
                    model.hasPathReady = true
                    dataSource.submitRequest(RestoreBackupRequest.RestoreMailbox(model.backupFilePath, model.passphrase))
                }

            }
            is RestoreBackupResult.DownloadBackup.Failure -> {
                scene.showBackupRetryLayout()
                scene.showMessage(result.message)
            }
        }
    }

    private fun onRestoreMailbox(result: RestoreBackupResult.RestoreMailbox){
        when(result){
            is RestoreBackupResult.RestoreMailbox.Success -> {
                host.exitToScene(MailboxParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.sync_complete)), true)
            }
            is RestoreBackupResult.RestoreMailbox.Progress -> scene.setProgress(result.progress)
            is RestoreBackupResult.RestoreMailbox.Failure -> {
                scene.showBackupRetryLayout()
                model.passphrase = null
                scene.enableRestoreButton(false)
                scene.showMessage(result.message)
            }
        }
    }

    inner class RestoreProgressListener : MediaHttpDownloaderProgressListener {
        override fun progressChanged(downloader: MediaHttpDownloader) {
            when (downloader.downloadState) {
                MediaHttpDownloader.DownloadState.MEDIA_IN_PROGRESS -> {
                    scene.setProgress((downloader.progress * 100).toInt())
                }
                MediaHttpDownloader.DownloadState.MEDIA_COMPLETE -> {
                    if(!model.hasPathReady && model.backupFilePath.isNotEmpty()) {
                        model.hasPathReady = true
                        scene.setProgress(60)
                        dataSource.submitRequest(RestoreBackupRequest.RestoreMailbox(model.backupFilePath, model.passphrase))
                    }
                }
            }
        }
    }


    override fun onStop() {
        websocketEvents.clearListener(webSocketEventListener)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        const val RETRY_TIME_DEFAULT = 5000L
        const val RETRY_TIME_DATA_READY = 10000L
        const val RETRY_TIMES_DEFAULT = 12
        const val RETRY_TIMES_DATA_READY = 18

        //Sync Process Percentages
        const val  SENDING_KEYS_PERCENTAGE = 10
        const val  WAITING_FOR_MAILBOX_PERCENTAGE = 40
        const val  DOWNLOADING_MAILBOX_PERCENTAGE = 70
        const val  SYNC_COMPLETE_PERCENTAGE = 100
    }
}