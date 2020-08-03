package com.criptext.mail.scenes.signup.customize

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.*
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.scenes.signup.customize.holder.CustomizeLayoutState
import com.criptext.mail.scenes.signup.customize.ui.CustomizeUIObserver
import com.criptext.mail.utils.*
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.validation.*
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.google.api.services.drive.Drive
import java.io.File


class CustomizeSceneController(
        private val model: CustomizeSceneModel,
        private val scene: CustomizeScene,
        private val keyboardManager: KeyboardManager,
        private val host : IHostActivity,
        private val storage: KeyValueStorage,
        private val activeAccount: ActiveAccount,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource): SceneController(host, null, storage) {

    override val menuResourceId: Int?
        get() = null

    val googleSignInListener = object: GoogleSignInObserver {
        override fun signInSuccess(drive: Drive){
            scene.setSubmitButtonState(ProgressButtonState.waiting)
            generalDataSource.submitRequest(GeneralRequest.SetCloudBackupActive(
                    CloudBackupData(
                            hasCloudBackup = true,
                            autoBackupFrequency = 0,
                            useWifiOnly = true,
                            fileSize = 0,
                            lastModified = null
                    )
            ))
        }

        override fun signInFailed(){
            scene.setSubmitButtonState(ProgressButtonState.enabled)
            scene.showError(UIMessage(R.string.login_fail_try_again_error_exception))
        }
    }

    private fun setBitmapOnProfileImage(imagePath: String){
        val file = File(imagePath)
        val bitmapImage = Utility.getBitmapFromFile(file)

        if(bitmapImage != null) {
            scene.showProfilePictureProgress(true)
            val exif = ExifInterface(file.path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> {
                }
            }
            val rotatedBitmap = Bitmap.createBitmap(bitmapImage, 0, 0,
                    bitmapImage.width, bitmapImage.height, matrix, true)
            generalDataSource.submitRequest(GeneralRequest.SetProfilePicture(rotatedBitmap))
            scene.updateProfilePicture(rotatedBitmap)
        }
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.resetLastMillisPin(storage)
        return when (activityMessage) {
            is ActivityMessage.ProfilePictureFile -> {
                if (activityMessage.filesMetadata.second != -1L) {
                    setBitmapOnProfileImage(activityMessage.filesMetadata.first)
                    true
                } else {
                    val resolver = host.getContentResolver()
                    if (resolver != null) {
                        scene.showPreparingFileDialog()
                        generalDataSource.submitRequest(GeneralRequest.GetRemoteFile(
                                listOf(activityMessage.filesMetadata.first), resolver)
                        )
                    }
                    true
                }
            }
            else -> false
        }
    }

    private val uiObserver: CustomizeUIObserver = object : CustomizeUIObserver(generalDataSource, host) {
        override fun onThemeSwitched() {
            model.hasDarkTheme = !model.hasDarkTheme
            storage.putBool(KeyValueStorage.StringKey.HasDarkTheme, model.hasDarkTheme)
            if (model.hasDarkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                host.setAppTheme(R.style.DarkAppTheme)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                host.setAppTheme(R.style.AppTheme)
            }
            scene.initLayout(model, this, activeAccount)
        }

        override fun onContactsSwitched(isChecked: Boolean) {
            if(isChecked) {
                scene.setSubmitButtonState(ProgressButtonState.waiting)
                if (host.checkPermissions(BaseActivity.RequestCode.readAccess.ordinal,
                                Manifest.permission.READ_CONTACTS)) {
                    storage.putBool(KeyValueStorage.StringKey.UserHasAcceptedPhonebookSync, true)
                    scene.showAwesomeText(true)
                    scene.setSubmitButtonState(ProgressButtonState.enabled)
                    val resolver = host.getContentResolver()
                    if (resolver != null)
                        generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
                }
            } else {
                scene.setSubmitButtonState(ProgressButtonState.disabled)
            }
        }

        override fun onNewCamPictureRequested() {
            scene.showProfilePictureProgress(true)
            host.launchExternalActivityForResult(ExternalActivityParams.Camera())
        }

        override fun onNewGalleryPictureRequested() {
            scene.showProfilePictureProgress(true)
            host.launchExternalActivityForResult(ExternalActivityParams.ProfileImagePicker())
        }

        override fun onNextButtonPressed() {
            when (model.state) {
                is CustomizeLayoutState.AccountCreated -> {
                    model.state = CustomizeLayoutState.ProfilePicture(activeAccount.name)
                    resetLayout(ProgressButtonState.enabled)
                }
                is CustomizeLayoutState.ProfilePicture -> {
                    if(model.hasSetPicture){
                        model.state = CustomizeLayoutState.DarkTheme()
                        resetLayout(ProgressButtonState.enabled)
                    } else {
                        if (host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            scene.showBottomDialog(this)
                        }
                    }
                }
                is CustomizeLayoutState.DarkTheme -> {
                    model.state = CustomizeLayoutState.Contacts(model.hasAllowedContacts)
                    resetLayout()
                    scene.setSubmitButtonState(ProgressButtonState.enabled)
                }
                is CustomizeLayoutState.Contacts -> {
                    model.state = CustomizeLayoutState.VerifyRecoveryEmail(model.recoveryEmail)
                    resetLayout()
                }
                is CustomizeLayoutState.VerifyRecoveryEmail -> {
                    if(model.isRecoveryEmailVerified) {
                        model.state = CustomizeLayoutState.CloudBackup()
                        resetLayout(ProgressButtonState.enabled)
                    } else {
                        generalDataSource.submitRequest(GeneralRequest.ResendConfirmationLink())
                        scene.setSubmitButtonState(ProgressButtonState.waiting)
                    }
                }
                is CustomizeLayoutState.CloudBackup -> {
                    scene.setSubmitButtonState(ProgressButtonState.waiting)
                    if(model.mDriveServiceHelper == null) model.mDriveServiceHelper = scene.getGoogleDriveService()
                    if(model.mDriveServiceHelper != null) {
                        generalDataSource.submitRequest(GeneralRequest.SetCloudBackupActive(
                                CloudBackupData(
                                        hasCloudBackup = true,
                                        autoBackupFrequency = 0,
                                        useWifiOnly = true,
                                        fileSize = 0,
                                        lastModified = null
                                )
                        ))
                    }else{
                        host.launchExternalActivityForResult(ExternalActivityParams.SignOutGoogleDrive())
                        host.launchExternalActivityForResult(ExternalActivityParams.SignInGoogleDrive())
                    }
                }
            }
        }

        override fun onSkipButtonPressed() {
            when (model.state) {
                is CustomizeLayoutState.ProfilePicture -> {
                    model.state = CustomizeLayoutState.DarkTheme()
                    resetLayout(ProgressButtonState.enabled)
                }
                is CustomizeLayoutState.DarkTheme -> {
                    model.state = CustomizeLayoutState.Contacts(model.hasAllowedContacts)
                    resetLayout()
                }
                is CustomizeLayoutState.Contacts -> {
                    model.state = CustomizeLayoutState.VerifyRecoveryEmail(model.recoveryEmail)
                    resetLayout()
                }
                is CustomizeLayoutState.VerifyRecoveryEmail -> {
                    if(model.isRecoveryEmailVerified) {
                        model.state = CustomizeLayoutState.CloudBackup()
                        resetLayout(ProgressButtonState.enabled)
                    } else {
                        scene.showSkipRecoveryEmailWarningDialog()
                    }
                }
                is CustomizeLayoutState.CloudBackup -> {
                    host.goToScene(
                            params = MailboxParams(showWelcome = true),
                            keep = false,
                            deletePastIntents = true,
                            activityMessage = null
                    )
                }
            }
        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result) {
                is DialogResult.DialogConfirmation -> {
                    model.state = CustomizeLayoutState.CloudBackup()
                    resetLayout(ProgressButtonState.enabled)
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

        override fun onBackButtonPressed() {
            this@CustomizeSceneController.onBackPressed()
        }
    }

    private val generalDataSourceListener = { result: GeneralResult ->
        when (result) {
            is GeneralResult.ResendConfirmationLink -> onResendConfirmationLink(result)
            is GeneralResult.SetProfilePicture -> onProfilePictureSet(result)
            is GeneralResult.SetCloudBackupActive -> onSetCloudBackupActive(result)
            is GeneralResult.SyncPhonebook -> onSyncPhonebook(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
        }
    }

    private fun onResendConfirmationLink(result: GeneralResult.ResendConfirmationLink){
        scene.setSubmitButtonState(ProgressButtonState.disabled)
        when(result) {
            is GeneralResult.ResendConfirmationLink.Success -> {
                scene.setupRecoveryEmailTimer()
            }
            is GeneralResult.ResendConfirmationLink.Failure -> {
                scene.showError(UIMessage(R.string.recovery_confirmation_resend_failed))
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        if(model.state == null) model.state = CustomizeLayoutState.AccountCreated(activeAccount.name, activeAccount.userEmail)
        model.hasDarkTheme = storage.getBool(KeyValueStorage.StringKey.HasDarkTheme, false)
        generalDataSource.listener = generalDataSourceListener
        websocketEvents.setListener(webSocketEventListener)
        scene.initLayout(
                model,
                uiObserver,
                activeAccount
        )
        scene.setSubmitButtonState(ProgressButtonState.enabled)
        return handleActivityMessage(activityMessage)
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    override fun onPause() {

    }

    override fun onStop() {
        generalDataSource.listener = null
        scene.uiObserver = null
    }

    override fun onNeedToSendEvent(event: Int) {
        return
    }

    private fun resetLayout(buttonState: ProgressButtonState = ProgressButtonState.disabled) {
        scene.setSubmitButtonState(buttonState)
        scene.initLayout(model, uiObserver, activeAccount)
    }

    override fun onBackPressed(): Boolean {
        return when (model.state) {
            is CustomizeLayoutState.AccountCreated,
            is CustomizeLayoutState.ProfilePicture -> {
                false
            }
            is CustomizeLayoutState.DarkTheme -> {
                model.state = CustomizeLayoutState.ProfilePicture(activeAccount.name)
                resetLayout(ProgressButtonState.enabled)
                false
            }
            is CustomizeLayoutState.Contacts -> {
                model.state = CustomizeLayoutState.DarkTheme()
                resetLayout(ProgressButtonState.enabled)
                false
            }
            is CustomizeLayoutState.VerifyRecoveryEmail -> {
                model.state = CustomizeLayoutState.Contacts(model.hasAllowedContacts)
                resetLayout(if(model.hasAllowedContacts) ProgressButtonState.enabled else ProgressButtonState.disabled)
                false
            }
            is CustomizeLayoutState.CloudBackup -> {
                model.state = CustomizeLayoutState.VerifyRecoveryEmail(model.recoveryEmail)
                resetLayout(if(model.isRecoveryEmailVerified) ProgressButtonState.enabled else ProgressButtonState.disabled)
                scene.setupRecoveryEmailTimer()
                false
            }
            else -> false
        }
    }

    private fun onProfilePictureSet(result: GeneralResult.SetProfilePicture){
        when(result) {
            is GeneralResult.SetProfilePicture.Success -> {
                scene.showProfilePictureProgress(false)
                model.hasSetPicture = true
                scene.initLayout(model, uiObserver, activeAccount)
                scene.changeToNextButton()
                scene.showError(UIMessage(R.string.profile_picture_updated))
            }
            is GeneralResult.SetProfilePicture.Failure -> {
                scene.showProfilePictureProgress(false)
                scene.showError(result.message)
            }
        }
    }

    private fun onGetRemoteFile(result: GeneralResult.GetRemoteFile) {
        when (result) {
            is GeneralResult.GetRemoteFile.Success -> {
                scene.dismissPreparingFileDialog()
                setBitmapOnProfileImage(result.remoteFiles.first().first)
            }
        }
    }

    private fun onSetCloudBackupActive(result: GeneralResult.SetCloudBackupActive){
        when(result){
            is GeneralResult.SetCloudBackupActive.Success -> {
                scene.removeFromScheduleCloudBackupJob(activeAccount.id)
                scene.scheduleCloudBackupJob(result.cloudBackupData.autoBackupFrequency, activeAccount.id, result.cloudBackupData.useWifiOnly)
                host.goToScene(
                        params = MailboxParams(showWelcome = true),
                        keep = false,
                        deletePastIntents = true,
                        activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.recommend_backup_sucess))
                )
            }
            is GeneralResult.SetCloudBackupActive.Failure -> {
                scene.setSubmitButtonState(ProgressButtonState.enabled)
                scene.showError(result.message)
            }
        }
    }

    private fun onSyncPhonebook(resultData: GeneralResult.SyncPhonebook){
        when (resultData) {
            is GeneralResult.SyncPhonebook.Success -> {
                scene.showError(UIMessage(R.string.sync_phonebook_text))
            }
        }
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {
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

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {

        }

        override fun onNewEvent(recipientId: String, domain: String) {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {
            if(model.state is CustomizeLayoutState.VerifyRecoveryEmail) {
                model.isRecoveryEmailVerified = true
                scene.updateRecoveryEmailVerification(model.isRecoveryEmailVerified)
                scene.changeToNextButton()
                scene.setSubmitButtonState(ProgressButtonState.enabled)
            }
        }
        override fun onDeviceLocked() {

        }

        override fun onDeviceRemoved() {

        }

        override fun onError(uiMessage: UIMessage) {

        }
    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            BaseActivity.RequestCode.readAccess.ordinal -> {
                val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.READ_CONTACTS }
                if (indexOfPermission < 0) return
                if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
                    scene.setSubmitButtonState(ProgressButtonState.disabled)
                    scene.updateContactSwitch(false)
                    scene.showError(UIMessage(R.string.sync_phonebook_permission))
                    return
                }
                scene.setSubmitButtonState(ProgressButtonState.enabled)
                storage.putBool(KeyValueStorage.StringKey.UserHasAcceptedPhonebookSync, true)
                scene.showAwesomeText(true)
                val resolver = host.getContentResolver()
                if (resolver != null)
                    generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
            }
            BaseActivity.RequestCode.writeAccess.ordinal -> {
                val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
                if (indexOfPermission < 0) return
                if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
                    scene.setSubmitButtonState(ProgressButtonState.enabled)
                    scene.showError(UIMessage(R.string.permission_filepicker_rationale))
                    return
                }
                scene.showBottomDialog(uiObserver)
            }
            else -> return
        }
    }
}
