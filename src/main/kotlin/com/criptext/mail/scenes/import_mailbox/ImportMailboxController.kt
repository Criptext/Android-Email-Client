package com.criptext.mail.scenes.import_mailbox

import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.RestoreBackupParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.params.SyncingParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton
import java.io.File

class ImportMailboxController(
        private val model: ImportMailboxModel,
        private val scene: ImportMailboxScene,
        private val host: IHostActivity,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource)
    : SceneController(host, activeAccount, storage){



    override val menuResourceId: Int? = null


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
            is GeneralResult.SyncBegin -> onSyncBegin(result)
        }
    }

    private val uiObserver = object: ImportMailboxUIObserver(generalDataSource, host){
        override fun onAnotherDevicePressed() {
            scene.showCheckingForDevicesDialog()
            generalDataSource.submitRequest(GeneralRequest.SyncBegin())
        }

        override fun onFromCloudPressed() {
            val gAccount = scene.getGoogleDriveService()
            if (gAccount == null){
                host.launchExternalActivityForResult(ExternalActivityParams.SignInGoogleDrive())
            } else {
                model.mDriveService = gAccount
                host.goToScene(RestoreBackupParams(), false)
            }
        }

        override fun onFromFilePressed() {
            host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
        }

        override fun onSkipPressed() {
            scene.showPasswordLoginDialog()
        }

        override fun onSkipContinuePressed() {
            host.goToScene(MailboxParams(), keep = false, deletePastIntents = true)
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

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onOkButtonPressed(password: String) {

        }

        override fun onCancelButtonPressed() {

        }

        override fun onSnackbarClicked() {

        }

    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(model = model, importUIObserver = uiObserver)
        generalDataSource.listener = generalDataSourceListener
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
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
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

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                generalDataSource.signalClient = SignalClient.Default(SignalStoreCriptext(generalDataSource.db, activeAccount))
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

    private fun onSyncBegin(result: GeneralResult.SyncBegin){
        scene.dismissCheckingForDevicesDialog()
        when(result) {
            is GeneralResult.SyncBegin.Success -> {
                host.goToScene(SyncingParams(), true)
            }
            is GeneralResult.SyncBegin.Failure -> {
                scene.showMessage(result.message)
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