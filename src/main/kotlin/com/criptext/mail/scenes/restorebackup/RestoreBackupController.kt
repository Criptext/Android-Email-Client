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
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.google.api.client.googleapis.media.MediaHttpDownloader
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener
import com.google.api.services.drive.Drive
import java.io.File

class RestoreBackupController(
        private val model: RestoreBackupModel,
        private val scene: RestoreBackupScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val websocketEvents: WebSocketEventPublisher,
        private val dataSource: BackgroundWorkManager<RestoreBackupRequest, RestoreBackupResult>,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>)
    : SceneController(){



    override val menuResourceId: Int? = null


    private val dataSourceListener: (RestoreBackupResult) -> Unit = { result ->
        when(result) {
            is RestoreBackupResult.CheckForBackup -> onCheckForBackup(result)
            is RestoreBackupResult.DownloadBackup -> onDownloadBackup(result)
        }
    }

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.RestoreMailbox -> onRestoreMailbox(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
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
        override fun onLocalProgressFinished() {
            host.exitToScene(MailboxParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.sync_complete)), true)
        }

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
            scene.showBackupFoundLayout(model.isFileEncrypted, model.isLocal)
            scene.enableRestoreButton(true)
        }

        override fun onChangeDriveAccount() {
            scene.enableRestoreButton(false)
            if(model.isLocal){
                host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
            } else {
                host.launchExternalActivityForResult(ExternalActivityParams.ChangeAccountGoogleDrive())
            }
        }

        override fun onRestore() {
            scene.showProgressLayout(model.isLocal)
            if(!model.isLocal) scene.setProgress(20)
            else scene.setProgress(0)
            if(model.backupFilePath.isNotEmpty()){
                generalDataSource.submitRequest(GeneralRequest.RestoreMailbox(model.backupFilePath, model.passphrase, model.isLocal))
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
        if(model.mDriveServiceHelper == null && !model.isLocal)
            host.exitToScene(MailboxParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.restore_backup_no_account)), true)

        scene.attachView(model = model, uiObserver = uiObserver)

        if(model.isLocal){
            setupModelFile(model.localFile!!.first)
        } else {
            dataSource.submitRequest(RestoreBackupRequest.CheckForBackup(model.mDriveServiceHelper!!))
        }

        model.accountEmail = activeAccount.userEmail

        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        return handleActivityMessage(activityMessage)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.resetLastMillisPin(storage)
        if (activityMessage is ActivityMessage.AddAttachments) {
            if(activityMessage.filesMetadata.isNotEmpty()){
                handleRestoreFile(activityMessage.filesMetadata)
            }
            return true
        }
        return false
    }

    private fun handleRestoreFile(filesMetadata: List<Pair<String, Long>>) {
        val file = filesMetadata.firstOrNull() ?: return
        if(file.second == -1L) {
            val resolver = host.getContentResolver()
            if(resolver != null) {
                scene.showPreparingFileDialog()
                generalDataSource.submitRequest(GeneralRequest.GetRemoteFile(
                        listOf(file.first), resolver)
                )
            }
        } else {
            setupModelFile(file.first)

        }
    }

    private fun setupModelFile(filePath: String){
        val file = File(filePath)
        if(file.extension !in listOf(UserDataWriter.FILE_ENCRYPTED_EXTENSION, UserDataWriter.FILE_UNENCRYPTED_EXTENSION,
                        UserDataWriter.FILE_GZIP_EXTENSION, UserDataWriter.FILE_TXT_EXTENSION)){
            scene.showMessage(UIMessage(R.string.restore_backup_bad_file))
            scene.showBackupNotFoundLayout(model.isLocal)
        } else {
            model.backupFilePath = filePath
            model.lastModified = file.lastModified()
            model.backupSize = file.length()
            model.isFileEncrypted = file.extension == UserDataWriter.FILE_ENCRYPTED_EXTENSION
            scene.enableRestoreButton(!model.isFileEncrypted)
            if(model.isLocal && !model.isFileEncrypted) {
                scene.showProgressLayout(model.isLocal)
                scene.setProgress(0)
                generalDataSource.submitRequest(GeneralRequest.RestoreMailbox(model.backupFilePath, model.passphrase, model.isLocal))
            } else {
                scene.showBackupFoundLayout(model.isFileEncrypted, model.isLocal)
                scene.updateFileData(model.backupSize, model.lastModified, model.isLocal)
            }
        }
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {

        }

        override fun onSyncDeviceDismiss(accountEmail: String) {

        }

        override fun onAccountSuspended(accountEmail: String) {

        }

        override fun onAccountUnsuspended(accountEmail: String) {

        }

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

        override fun onNewEvent(recipientId: String, domain: String) {

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
                model.backupSize = result.fileSize
                model.isFileEncrypted = result.isEncrypted
                scene.enableRestoreButton(!result.isEncrypted)
                scene.showBackupFoundLayout(model.isFileEncrypted, model.isLocal)
                scene.updateFileData(model.backupSize, model.lastModified, model.isLocal)
            }
            is RestoreBackupResult.CheckForBackup.Failure -> {
                scene.showBackupNotFoundLayout(model.isLocal)
            }
        }
    }

    private fun onDownloadBackup(result: RestoreBackupResult.DownloadBackup){
        when(result){
            is RestoreBackupResult.DownloadBackup.Success -> {
                model.backupFilePath = result.filePath
                if(!model.hasPathReady){
                    model.hasPathReady = true
                    generalDataSource.submitRequest(GeneralRequest.RestoreMailbox(model.backupFilePath, model.passphrase))
                }

            }
            is RestoreBackupResult.DownloadBackup.Failure -> {
                scene.showBackupRetryLayout(model.isLocal)
                scene.showMessage(result.message)
            }
        }
    }

    private fun onRestoreMailbox(result: GeneralResult.RestoreMailbox){
        when(result){
            is GeneralResult.RestoreMailbox.Success -> {
                if(model.isLocal) {
                    scene.localPercentageAnimation()
                } else {
                    host.exitToScene(MailboxParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.sync_complete)), true)
                }
            }
            is GeneralResult.RestoreMailbox.Progress -> scene.setProgress(result.progress)
            is GeneralResult.RestoreMailbox.SyncError -> {
                scene.showMessage(result.message)
                if(model.isLocal)
                    scene.showBackupNotFoundLayout(model.isLocal)
                else
                    scene.showBackupRetryLayout(model.isLocal)
            }
            is GeneralResult.RestoreMailbox.Failure -> {
                scene.showBackupRetryLayout(model.isLocal)
                model.passphrase = null
                scene.enableRestoreButton(false)
                scene.showMessage(result.message)
            }
        }
    }

    private fun onGetRemoteFile(result: GeneralResult.GetRemoteFile) {
        when(result){
            is GeneralResult.GetRemoteFile.Success -> {
                scene.dismissPreparingFileDialog()
                val file = result.remoteFiles.first()
                setupModelFile(file.first)
            }
            is GeneralResult.GetRemoteFile.Failure -> {

            }
        }
    }

    inner class RestoreProgressListener : MediaHttpDownloaderProgressListener {
        override fun progressChanged(downloader: MediaHttpDownloader) {
            when (downloader.downloadState) {
                MediaHttpDownloader.DownloadState.MEDIA_IN_PROGRESS -> {
                    host.runOnUiThread(Runnable {
                        scene.setProgress((downloader.progress * 100).toInt())
                    })
                }
                MediaHttpDownloader.DownloadState.MEDIA_COMPLETE -> {
                    if(!model.hasPathReady && model.backupFilePath.isNotEmpty()) {
                        model.hasPathReady = true
                        generalDataSource.submitRequest(GeneralRequest.RestoreMailbox(model.backupFilePath, model.passphrase))
                        host.runOnUiThread(Runnable {
                            scene.setProgress(60)
                        })
                    }
                }
                else -> {}
            }
        }
    }

    override fun onPause() {
        cleanup()
    }

    override fun onStop() {
        cleanup()
    }

    override fun onNeedToSendEvent(event: Int) {
        generalDataSource.submitRequest(GeneralRequest.UserEvent(event))
    }

    private fun cleanup(){
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
}