package com.criptext.mail.scenes.settings.cloudbackup

import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.RestoreBackupParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.cloudbackup.data.*
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.services.data.JobIdData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.ActivityTransitionAnimationData
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.services.drive.Drive
import java.io.File
import java.io.IOException


class CloudBackupController(
        private val model: CloudBackupModel,
        private val scene: CloudBackupScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: CloudBackupDataSource)
    : SceneController(host, activeAccount, storage){



    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
        }
    }

    private val dataSourceListener: (CloudBackupResult) -> Unit = { result ->
        when(result) {
            is CloudBackupResult.SetCloudBackupActive -> onSetCloudBackupActive(result)
            is CloudBackupResult.LoadCloudBakcupData -> onLoadCloudBackupData(result)
            is CloudBackupResult.UploadBackupToDrive -> onUploadBackupToDrive(result)
            is CloudBackupResult.DataFileCreation -> onDataFileCreated(result)
            is CloudBackupResult.SaveFileInLocalStorage -> onSaveFileInLocalStorage(result)
        }
    }

    private val progressListener = CloudBackupProgressListener()

    val googleSignInListener = object: GoogleSignInObserver {
        override fun signInSuccess(drive: Drive){
            model.mDriveService = drive
            uiObserver.backUpNowPressed()
        }

        override fun signInFailed(){
            scene.setCloudBackupSwitchState(false)
            scene.showMessage(UIMessage(R.string.login_fail_try_again_error_exception))
        }
    }

    private val uiObserver = object: CloudBackupUIObserver(generalDataSource, host){
        override fun exportBackupPressed() {
            scene.showEncryptBackupDialog(this)
        }

        override fun restoreBackupPressed() {
            host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                        is DialogType.SignIn ->
                            host.goToScene(SignInParams(true), true)
                    }
                }
            }
        }

        override fun onOkButtonPressed(password: String) {

        }

        override fun onCancelButtonPressed() {

        }

        override fun onSnackbarClicked() {

        }

        override fun backUpNowPressed() {
            scene.backingUpNow(true)
            if(model.mDriveService == null) model.mDriveService = scene.getGoogleDriveService()
            if(model.mDriveService != null) {
                dataSource.submitRequest(CloudBackupRequest.DataFileCreation(null))
                scene.showProgressDialog()
            }else{
                host.launchExternalActivityForResult(ExternalActivityParams.SignOutGoogleDrive())
                host.launchExternalActivityForResult(ExternalActivityParams.SignInGoogleDrive())
            }
        }

        override fun onPasswordChangedListener(password: String) {
            if(password.isNotEmpty() && password.length >= 3) {
                model.passphraseForEncryptedFile = password
                scene.enableSaveButtonOnDialog()
            }else{
                model.passphraseForEncryptedFile = null
                scene.disableSaveButtonOnDialog()
            }
        }

        override fun setOnCheckedChangeListener(isChecked: Boolean) {
            if(!isChecked){
                model.passphraseForEncryptedFile = null
            }else{
                scene.disableSaveButtonOnDialog()
            }
        }

        override fun encryptDialogButtonPressed() {
            dataSource.submitRequest(CloudBackupRequest.DataFileCreation(model.passphraseForEncryptedFile, isLocal = true))
            scene.showProgressDialog()
        }

        override fun onFrequencyChanged(frequency: Int) {
            dataSource.submitRequest(CloudBackupRequest.SetCloudBackupActive(
                    CloudBackupData(
                            hasCloudBackup = model.hasCloudBackup,
                            autoBackupFrequency = frequency,
                            useWifiOnly = model.wifiOnly,
                            fileSize = model.lastBackupSize,
                            lastModified = model.lastTimeBackup
                    )
            ))
        }

        override fun onWifiOnlySwitched(isActive: Boolean) {
            dataSource.submitRequest(CloudBackupRequest.SetCloudBackupActive(
                    CloudBackupData(
                            hasCloudBackup = model.hasCloudBackup,
                            autoBackupFrequency = model.autoBackupFrequency,
                            useWifiOnly = isActive,
                            fileSize = model.lastBackupSize,
                            lastModified = model.lastTimeBackup
                    )
            ))
        }

        override fun onChangeGoogleDriveAccount() {
            host.launchExternalActivityForResult(ExternalActivityParams.ChangeAccountGoogleDrive())
        }

        override fun onCloudBackupActivated(isActive: Boolean) {
            dataSource.submitRequest(CloudBackupRequest.SetCloudBackupActive(
                    CloudBackupData(
                            hasCloudBackup = isActive,
                            autoBackupFrequency = model.autoBackupFrequency,
                            useWifiOnly = model.wifiOnly,
                            fileSize = model.lastBackupSize,
                            lastModified = model.lastTimeBackup
                    )
            ))
        }

        override fun onBackButtonPressed() {
            host.goToScene(
                    params = SettingsParams(),
                    activityMessage = null,
                    animationData = ActivityTransitionAnimationData(
                            forceAnimation = true,
                            enterAnim = 0,
                            exitAnim = R.anim.slide_out_right
                    ),
                    keep = false
            )
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        websocketEvents.setListener(webSocketEventListener)
        model.activeAccountEmail = activeAccount.userEmail
        scene.attachView(model = model, cloudBackupUIObserver1 = uiObserver)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        scene.showLoadingSettingsDialog()
        dataSource.submitRequest(CloudBackupRequest.LoadCloudBackupData(model.mDriveService))
        return handleActivityMessage(activityMessage)
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
            val localFile = File(file.first)
            if (localFile.extension !in listOf(UserDataWriter.FILE_ENCRYPTED_EXTENSION, UserDataWriter.FILE_UNENCRYPTED_EXTENSION,
                            UserDataWriter.FILE_GZIP_EXTENSION)) {
                scene.showMessage(UIMessage(R.string.restore_backup_bad_file))
            } else {
                val isFileEncrypted = localFile.extension == UserDataWriter.FILE_ENCRYPTED_EXTENSION
                host.goToScene(
                        params = RestoreBackupParams(true, Pair(file.first, isFileEncrypted)),
                        activityMessage = null,
                        keep = false, deletePastIntents = true
                )
            }
        }
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.resetLastMillisPin(storage)
        if (activityMessage is ActivityMessage.AddAttachments) {
            if(activityMessage.filesMetadata.isNotEmpty()){
                handleRestoreFile(activityMessage.filesMetadata)
            }
            return true
        }
        if(activityMessage is ActivityMessage.SaveFileToLocalStorage){
            if(model.localFilePath != null) {
                dataSource.submitRequest(CloudBackupRequest.SaveFileInLocalStorage(
                        contentResolver = host.getContentResolver()!!,
                        filePath = model.localFilePath!!,
                        fileUri = activityMessage.uri
                ))
                model.localFilePath = null
            }
            return true
        }
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onDataFileCreated(result: CloudBackupResult.DataFileCreation){
        when(result){
            is CloudBackupResult.DataFileCreation.Success -> {
                scene.setProgressDialog(100)
                scene.hideProgressDialog()
                if(result.isLocal){
                    model.localFilePath = result.filePath
                    host.launchExternalActivityForResult(
                            ExternalActivityParams.ExportBackupFile(result.filePath, result.isEncrypted))
                } else {
                    if (model.mDriveService != null) {
                        scene.showUploadProgressBar(true)
                        scene.setUploadProgress(0)
                        dataSource.submitRequest(CloudBackupRequest.UploadBackupToDrive(result.filePath,
                                model.mDriveService!!, progressListener))
                    } else {
                        scene.backingUpNow(false)
                    }
                }
            }
            is CloudBackupResult.DataFileCreation.Progress -> {
                scene.setProgressDialog(result.progress)
            }
            is CloudBackupResult.DataFileCreation.Failure -> {
                scene.backingUpNow(false)
                scene.hideProgressDialog()
                scene.showMessage(result.message)
            }
        }
    }

    private fun onSaveFileInLocalStorage(result: CloudBackupResult.SaveFileInLocalStorage){
        when(result){
            is CloudBackupResult.SaveFileInLocalStorage.Success -> {
                scene.showMessage(UIMessage(R.string.export_backup_file_ready))
            }
            is CloudBackupResult.SaveFileInLocalStorage.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    private fun onSetCloudBackupActive(result: CloudBackupResult.SetCloudBackupActive){
        when(result){
            is CloudBackupResult.SetCloudBackupActive.Success -> {
                model.wifiOnly = result.cloudBackupData.useWifiOnly
                if(model.autoBackupFrequency != result.cloudBackupData.autoBackupFrequency){
                    scene.removeFromScheduleCloudBackupJob(activeAccount.id)
                    model.autoBackupFrequency = result.cloudBackupData.autoBackupFrequency
                    scene.scheduleCloudBackupJob(model.autoBackupFrequency, activeAccount.id, model.wifiOnly)
                }
                if(model.hasCloudBackup != result.cloudBackupData.hasCloudBackup){
                    if(result.cloudBackupData.hasCloudBackup) {
                        model.hasCloudBackup = result.cloudBackupData.hasCloudBackup
                        if(model.autoBackupFrequency != 3) {
                            scene.removeFromScheduleCloudBackupJob(activeAccount.id)
                            scene.scheduleCloudBackupJob(model.autoBackupFrequency, activeAccount.id, model.wifiOnly)
                        } else
                            scene.removeFromScheduleCloudBackupJob(activeAccount.id)
                        host.launchExternalActivityForResult(ExternalActivityParams.SignInGoogleDrive())

                    } else {
                        model.hasCloudBackup = result.cloudBackupData.hasCloudBackup
                        scene.removeFromScheduleCloudBackupJob(activeAccount.id)
                        val savedCloudDataString = storage.getString(KeyValueStorage.StringKey.SavedBackupData, "")
                        val savedCloudData = if(savedCloudDataString.isNotEmpty()) SavedCloudData.fromJson(savedCloudDataString)
                        else listOf()
                        val mutableSavedData = mutableListOf<SavedCloudData>()
                        mutableSavedData.addAll(savedCloudData)
                        val data = mutableSavedData.find { it.accountId == activeAccount.id }
                        if(data != null) mutableSavedData.remove(data)
                        storage.putString(KeyValueStorage.StringKey.SavedBackupData, SavedCloudData.toJSON(mutableSavedData).toString())
                        host.launchExternalActivityForResult(ExternalActivityParams.SignOutGoogleDrive())
                    }
                }
                val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
                val listOfJobs = if(savedJobsString.isEmpty()) mutableListOf()
                else JobIdData.fromJson(savedJobsString)
                val accountSavedData = listOfJobs.find { it.accountId == activeAccount.id}
                if(accountSavedData != null) {
                    listOfJobs.remove(accountSavedData)
                    accountSavedData.useWifiOnly = model.wifiOnly
                    listOfJobs.add(accountSavedData)
                    storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())
                }

            }
            is CloudBackupResult.SetCloudBackupActive.Failure -> {
                scene.setCloudBackupSwitchState(!result.cloudBackupData.hasCloudBackup)
            }
        }
    }

    private fun onLoadCloudBackupData(result: CloudBackupResult.LoadCloudBakcupData){
        scene.dismissLoadingSettingsDialog()
        when(result){
            is CloudBackupResult.LoadCloudBakcupData.Success -> {
                model.autoBackupFrequency = result.cloudBackupData.autoBackupFrequency
                model.hasCloudBackup = result.cloudBackupData.hasCloudBackup
                model.wifiOnly = result.cloudBackupData.useWifiOnly
                model.lastBackupSize = result.cloudBackupData.fileSize
                model.lastTimeBackup = result.cloudBackupData.lastModified
                scene.updateCloudBackupData(model)
            }
            is CloudBackupResult.LoadCloudBakcupData.Failure -> {
                model.autoBackupFrequency = result.cloudBackupData.autoBackupFrequency
                model.hasCloudBackup = result.cloudBackupData.hasCloudBackup
                model.wifiOnly = result.cloudBackupData.useWifiOnly
                scene.updateCloudBackupData(model)
            }
        }
    }

    private fun onUploadBackupToDrive(result: CloudBackupResult.UploadBackupToDrive){
        when(result){
            is CloudBackupResult.UploadBackupToDrive.Success -> {
                model.hasOldFile = result.hasOldFile
                model.oldFileId = result.oldFileIds
                model.fileLength = result.fileLength
                model.lastTimeBackup = result.lastModified
                scene.backingUpNow(false)
                scene.updateFileInfo(model.fileLength, model.lastTimeBackup)
                if(model.hasOldFile && model.isBackupDone) {
                    model.isBackupDone = false
                    dataSource.submitRequest(CloudBackupRequest.DeleteFileInDrive(model.mDriveService!!, model.oldFileId))
                    model.hasOldFile = false
                }
            }
            is CloudBackupResult.UploadBackupToDrive.Progress -> {
                scene.setUploadProgress(result.progress)
            }
            is CloudBackupResult.UploadBackupToDrive.Failure -> {
                scene.backingUpNow(false)
                scene.showUploadProgressBar(false)
                scene.showMessage(result.message)
            }
        }
    }

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                generalDataSource.signalClient = SignalClient.Default(SignalStoreCriptext(generalDataSource.db, activeAccount))
                dataSource.activeAccount = activeAccount
                val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
                websocketEvents = if(jwts.isNotEmpty())
                    WebSocketSingleton.getInstance(jwts)
                else
                    WebSocketSingleton.getInstance(activeAccount.jwt)

                websocketEvents.setListener(webSocketEventListener)

                scene.dismissAccountSuspendedDialog()

                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))

                host.goToScene(
                        params = MailboxParams(),
                        activityMessage = null,
                        keep = false,
                        deletePastIntents = true
                )
            }
        }
    }

    private fun onGetRemoteFile(result: GeneralResult.GetRemoteFile) {
        when (result) {
            is GeneralResult.GetRemoteFile.Success -> {
                scene.dismissPreparingFileDialog()
                val file = result.remoteFiles.first()
                if(File(file.first).extension !in listOf(UserDataWriter.FILE_ENCRYPTED_EXTENSION, UserDataWriter.FILE_UNENCRYPTED_EXTENSION,
                                UserDataWriter.FILE_GZIP_EXTENSION, UserDataWriter.FILE_TXT_EXTENSION)){
                    scene.showMessage(UIMessage(R.string.restore_backup_bad_file))
                } else {
                    host.goToScene(
                            params = RestoreBackupParams(true, Pair(file.first, false)),
                            activityMessage = null,
                            keep = false, deletePastIntents = true
                    )
                }
            }
        }
    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(uiObserver, activeAccount.userEmail, dialogType)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {

        }

        override fun onSyncDeviceDismiss(accountEmail: String) {

        }

        override fun onAccountSuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail) {
                    showSuspendedAccountDialog()
                }
            })
        }

        override fun onAccountUnsuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    scene.dismissAccountSuspendedDialog()
            })
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

    private fun updateUploadDone(){
        scene.setUploadProgress(100)
        scene.showUploadProgressBar(false)
        scene.backingUpNow(false)
        scene.checkCloudBackupIcon()
        scene.updateFileInfo(model.fileLength, model.lastTimeBackup)
    }

    inner class CloudBackupProgressListener: MediaHttpUploaderProgressListener {
        @Throws(IOException::class)
        override fun progressChanged(uploader: MediaHttpUploader) {
            when (uploader.uploadState) {
                MediaHttpUploader.UploadState.INITIATION_STARTED -> {
                    host.runOnUiThread(Runnable {
                        scene.setUploadProgress(20)
                    })
                }
                MediaHttpUploader.UploadState.INITIATION_COMPLETE -> {
                    host.runOnUiThread(Runnable {
                        scene.setUploadProgress(40)
                    })
                }
                MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {
                    host.runOnUiThread(Runnable {
                        scene.setUploadProgress((uploader.progress * 100).toInt())
                    })
                }
                MediaHttpUploader.UploadState.MEDIA_COMPLETE -> {
                    model.isBackupDone = true
                    if(model.hasOldFile) {
                        dataSource.submitRequest(CloudBackupRequest.DeleteFileInDrive(model.mDriveService!!, model.oldFileId))
                        model.hasOldFile = false
                    }
                    host.runOnUiThread(Runnable {
                        updateUploadDone()
                    })
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
        uiObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }
}