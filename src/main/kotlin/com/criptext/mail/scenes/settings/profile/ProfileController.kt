package com.criptext.mail.scenes.settings.profile

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.profile.data.ProfileRequest
import com.criptext.mail.scenes.settings.profile.data.ProfileResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.MemoryPolicy
import java.lang.Exception


class ProfileController(
        private val activeAccount: ActiveAccount,
        private val model: ProfileModel,
        private val scene: ProfileScene,
        private val host: IHostActivity,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val websocketEvents: WebSocketEventPublisher,
        private val dataSource: BackgroundWorkManager<ProfileRequest, ProfileResult>,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ChangeContactName -> onContactNameChanged(result)
        }
    }

    private val dataSourceListener: (ProfileResult) -> Unit = { result ->
        when(result) {
            is ProfileResult.SetProfilePicture -> onProfilePictureSet(result)
            is ProfileResult.DeleteProfilePicture -> onProfilePictureDeleted(result)
        }
    }

    private val uiObserver = object: ProfileUIObserver{
        override fun onProfileNameChanged(name: String) {
            keyboardManager.hideKeyboard()
            activeAccount.updateFullName(storage, name)
            model.name = name
            generalDataSource.submitRequest(GeneralRequest.ChangeContactName(
                    fullName = name,
                    recipientId = activeAccount.recipientId
            ))
        }

        override fun onEditPicturePressed() {
            if(host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                scene.showBottomDialog(this)
            }
        }

        override fun onEditProfileNamePressed() {
            scene.showProfileNameDialog(model.name)
        }

        override fun onNewCamPictureRequested() {
            scene.showProfilePictureProgress()
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.Camera())
        }

        override fun onNewGalleryPictureRequested() {
            scene.showProfilePictureProgress()
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.ImagePicker(1))
        }

        override fun onDeletePictureRequested() {
            scene.showProfilePictureProgress()
            dataSource.submitRequest(ProfileRequest.DeleteProfilePicture())
        }

        override fun onSnackbarClicked() {

        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.SyncDenied(trustedDeviceInfo))
        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {

        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,true)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)

        scene.attachView(uiObserver, activeAccount.recipientId, model)
        generalDataSource.listener = generalDataSourceListener
        dataSource.listener = dataSourceListener

        return handleActivityMessage(activityMessage)
    }

    private fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.exitToScene(LinkingParams(activeAccount.userEmail, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), null,
                        false, true)
            }
            is GeneralResult.LinkAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onSyncAccept(resultData: GeneralResult.SyncAccept){
        when (resultData) {
            is GeneralResult.SyncAccept.Success -> {
                host.exitToScene(LinkingParams(activeAccount.userEmail, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), ActivityMessage.SyncMailbox(),
                        false, true)
            }
            is GeneralResult.SyncAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onPasswordChangedRemotely(result: GeneralResult.ConfirmPassword){
        when (result) {
            is GeneralResult.ConfirmPassword.Success -> {
                scene.dismissConfirmPasswordDialog()
                scene.showMessage(UIMessage(R.string.update_password_success))
            }
            is GeneralResult.ConfirmPassword.Failure -> {
                scene.setConfirmPasswordError(result.message)
            }
        }
    }

    private fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        when (result) {
            is GeneralResult.DeviceRemoved.Success -> {
                host.exitToScene(SignInParams(),
                        ActivityMessage.ShowUIMessage(
                                UIMessage(R.string.device_removed_remotely_exception)),
                        true, true)
            }
        }
    }

    private fun onContactNameChanged(result: GeneralResult.ChangeContactName){
        when(result) {
            is GeneralResult.ChangeContactName.Success -> {
                scene.updateProfileName(result.fullName)
                scene.showMessage(UIMessage(R.string.profile_name_updated))
            }
            is GeneralResult.ChangeContactName.Failure -> {
                scene.showMessage(UIMessage(R.string.error_updating_account))
            }
        }
    }

    private fun onProfilePictureSet(result: ProfileResult.SetProfilePicture){
        when(result) {
            is ProfileResult.SetProfilePicture.Success -> {
                Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.recipientId}"))
                scene.hideProfilePictureProgress()
                scene.showMessage(UIMessage(R.string.profile_picture_updated))
            }
            is ProfileResult.SetProfilePicture.Failure -> {
                scene.resetProfilePicture(model.name)
                scene.hideProfilePictureProgress()
                scene.showMessage(UIMessage(R.string.profile_picture_update_failed))
            }
        }
    }

    private fun onProfilePictureDeleted(result: ProfileResult.DeleteProfilePicture){
        scene.hideProfilePictureProgress()
        when(result) {
            is ProfileResult.DeleteProfilePicture.Success -> {
                scene.resetProfilePicture(model.name)
                scene.showMessage(UIMessage(R.string.profile_picture_deleted))
            }
            is ProfileResult.DeleteProfilePicture.Failure -> {
                scene.showMessage(UIMessage(R.string.profile_picture_delete_failed))
            }
        }
    }

    override fun onStop() {
        websocketEvents.clearListener(webSocketEventListener)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.resetLastMillisPin()
        PinLockUtils.setPinLockTimeoutPosition(storage.getInt(KeyValueStorage.StringKey.PINTimeout, 1))
        if (activityMessage is ActivityMessage.ProfilePictureFile) {
            scene.showProfilePictureProgress()
            val exif = ExifInterface(activityMessage.path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> {
                }
            }
            val rotatedBitmap = Bitmap.createBitmap(activityMessage.image, 0, 0,
                    activityMessage.image.width, activityMessage.image.height, matrix, true)
            dataSource.submitRequest(ProfileRequest.SetProfilePicture(rotatedBitmap))
            scene.updateProfilePicture(rotatedBitmap)
            return true
        }
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

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onNewEvent() {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                scene.showConfirmPasswordDialog(uiObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }

    override fun onBackPressed(): Boolean {
        uiObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != BaseActivity.RequestCode.writeAccess.ordinal) return

        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
        if (indexOfPermission < 0) return
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
            scene.showMessage(UIMessage(R.string.permission_filepicker_rationale))
            return
        }
        scene.showBottomDialog(uiObserver)
    }
}