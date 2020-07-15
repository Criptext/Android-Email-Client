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
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.profile.data.*
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.*
import com.criptext.mail.utils.eventhelper.ParsedEvent
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.TransitionAnimationData
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton
import com.squareup.picasso.Picasso
import java.io.File


class ProfileController(
        private var activeAccount: ActiveAccount,
        private val model: ProfileModel,
        private val scene: ProfileScene,
        private val host: IHostActivity,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private var websocketEvents: WebSocketEventPublisher,
        private val dataSource: ProfileDataSource,
        private val generalDataSource: GeneralDataSource)
    : SceneController(host, activeAccount, storage){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ChangeContactName -> onContactNameChanged(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
            is GeneralResult.DeleteAccount -> onDeleteAccount(result)
            is GeneralResult.Logout -> customOnLogout(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.GetUserSettings -> onGetUserSettings(result)
            is GeneralResult.ActiveAccountUpdateMailbox -> onUpdateMailbox(result)
        }
    }

    private val dataSourceListener: (ProfileResult) -> Unit = { result ->
        when(result) {
            is ProfileResult.SetProfilePicture -> onProfilePictureSet(result)
            is ProfileResult.DeleteProfilePicture -> onProfilePictureDeleted(result)
        }
    }

    private val uiObserver = object: ProfileUIObserver(generalDataSource, host) {
        override fun onCriptextFooterSwitched(isChecked: Boolean) {
            val footerData = ProfileFooterData(activeAccount.id, isChecked)
            val allFooterData = storage.getString(KeyValueStorage.StringKey.ShowCriptextFooter, "")
            if (allFooterData.isNotEmpty()) {
                val savedData = ProfileFooterData.fromJson(allFooterData)
                val findAccountFooterData = savedData.find { it.accountId == activeAccount.id }
                if (findAccountFooterData != null) {
                    savedData.remove(findAccountFooterData)
                }
                savedData.add(footerData)
                storage.putString(KeyValueStorage.StringKey.ShowCriptextFooter, ProfileFooterData.toJSON(savedData).toString())
            } else {
                val json = ProfileFooterData.toJSON(listOf(footerData))
                storage.putString(KeyValueStorage.StringKey.ShowCriptextFooter, json.toString())
            }
        }

        override fun onLogoutClicked() {
            scene.showLogoutDialog(model.userData.isLastDeviceWith2FA)
        }

        override fun onLogoutConfirmedClicked() {
            scene.showMessageAndProgressDialog(UIMessage(R.string.login_out_dialog_message))
            generalDataSource.submitRequest(GeneralRequest.Logout(false, true))
        }

        override fun onDeleteAccountClicked() {
            scene.showGeneralDialogWithInputPassword(DialogData.DialogMessageData(
                    title = UIMessage(R.string.delete_account_dialog_title),
                    message = listOf(UIMessage(R.string.delete_account_dialog_message)),
                    type = DialogType.DeleteAccount()
            ))
        }

        override fun onRecoveryEmailOptionClicked() {
            val message = if(model.comesFromMailbox) ActivityMessage.ComesFromMailbox() else null
            host.goToScene(RecoveryEmailParams(model.userData), false, activityMessage = message)
        }

        override fun onReplyToChangeClicked() {
            val message = if(model.comesFromMailbox) ActivityMessage.ComesFromMailbox() else null
            host.goToScene(ReplyToParams(model.userData), false, activityMessage = message)
        }

        override fun onChangePasswordOptionClicked() {
            host.goToScene(ChangePasswordParams(), true)
        }

        override fun onSignatureOptionClicked() {
            host.goToScene(SignatureParams(activeAccount.recipientId, activeAccount.domain), true)
        }

        override fun onProfileNameChanged(name: String) {
            keyboardManager.hideKeyboard()
            model.userData.name = name
            generalDataSource.submitRequest(GeneralRequest.ChangeContactName(
                    fullName = name,
                    recipientId = activeAccount.recipientId,
                    domain = activeAccount.domain
            ))
        }

        override fun onEditPicturePressed() {
            if(host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                scene.showBottomDialog(this)
            }
        }

        override fun onEditProfileNamePressed() {
            scene.showProfileNameDialog(model.userData.name)
        }

        override fun onNewCamPictureRequested() {
            scene.showProfilePictureProgress()
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.Camera())
        }

        override fun onNewGalleryPictureRequested() {
            scene.showProfilePictureProgress()
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.ProfileImagePicker())
        }

        override fun onDeletePictureRequested() {
            scene.showProfilePictureProgress()
            dataSource.submitRequest(ProfileRequest.DeleteProfilePicture())
        }

        override fun onSnackbarClicked() {

        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogWithInput -> {
                    when(result.type){
                        is DialogType.DeleteAccount -> {
                            scene.toggleGeneralDialogLoad(true)
                            generalDataSource.submitRequest(GeneralRequest.DeleteAccount(result.textInput))
                        }
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
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            if(model.comesFromMailbox){
                host.goToScene(
                        params = MailboxParams(),
                        activityMessage = null,
                        animationData = TransitionAnimationData(
                                forceAnimation = true,
                                enterAnim = 0,
                                exitAnim = R.anim.slide_out_right
                        ),
                        deletePastIntents = true,
                        keep = false
                )
            } else {
                host.goToScene(
                        params = SettingsParams(),
                        activityMessage = null,
                        animationData = TransitionAnimationData(
                                forceAnimation = true,
                                enterAnim = 0,
                                exitAnim = R.anim.slide_out_right
                        ),
                        keep = false
                )
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        checkFirstTimeToResetCache()
        websocketEvents.setListener(webSocketEventListener)

        model.userData = ProfileUserData.getDefaultData(activeAccount)
        model.criptextFooterEnabled = if(storage.getString(KeyValueStorage.StringKey.ShowCriptextFooter, "").isNotEmpty()){
            val footerData = ProfileFooterData.fromJson(storage.getString(KeyValueStorage.StringKey.ShowCriptextFooter, ""))
            footerData.find { it.accountId == activeAccount.id }?.hasFooterEnabled ?: true
        } else {
            true
        }
        scene.attachView(uiObserver, activeAccount, model)
        scene.showPlusBadge(activeAccount.type)
        if(activeAccount.domain != Contact.mainDomain)
            scene.hideFooterSwitch()
        scene.enableProfileSettings(false)
        generalDataSource.listener = generalDataSourceListener
        dataSource.listener = dataSourceListener
        generalDataSource.submitRequest(GeneralRequest.GetUserSettings())

        return handleActivityMessage(activityMessage)
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun checkFirstTimeToResetCache(){
        if(!storage.getBool(KeyValueStorage.StringKey.HasTimestampForCacheReset, false)){
            storage.putBool(KeyValueStorage.StringKey.HasTimestampForCacheReset, true)
            storage.putLong(KeyValueStorage.StringKey.CacheResetTimestamp, System.currentTimeMillis())
        }
    }

    private fun customOnLogout(result: GeneralResult.Logout){
        when(result) {
            is GeneralResult.Logout.Success -> {
                scene.dismissMessageAndProgressDialog()
                CloudBackupJobService.cancelJob(storage, result.oldAccountId)
                if(result.activeAccount == null)
                    host.goToScene(
                            params = SignInParams(),
                            activityMessage = null,
                            keep = false,
                            deletePastIntents = true
                    )
                else {
                    activeAccount = result.activeAccount

                    host.goToScene(
                            params = MailboxParams(),
                            activityMessage = ActivityMessage.LogoutAccount(result.oldAccountEmail, result.activeAccount),
                            keep = false, deletePastIntents = true
                    )
                }
            }
            is GeneralResult.Logout.Failure -> {
                scene.dismissMessageAndProgressDialog()
                scene.showMessage(UIMessage(R.string.error_login_out))
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
                        keep = false, deletePastIntents = true
                )
            }
        }
    }

    private fun onGetUserSettings(result: GeneralResult.GetUserSettings){
        when(result) {
            is GeneralResult.GetUserSettings.Success -> {
                val userData = ProfileUserData(
                        name = activeAccount.name,
                        email = activeAccount.userEmail,
                        replyToEmail = result.userSettings.replyTo,
                        recoveryEmail = result.userSettings.recoveryEmail,
                        isLastDeviceWith2FA = result.userSettings.devices.size == 1
                                && result.userSettings.hasTwoFA,
                        isEmailConfirmed = result.userSettings.recoveryEmailConfirmationState
                )
                model.userData = userData
                scene.updateCurrentEmailStatus(model.userData.isEmailConfirmed)
                scene.enableProfileSettings(true)
                scene.showPlusBadge(result.userSettings.customerType)
            }
            is GeneralResult.GetUserSettings.Failure -> {
                scene.showMessage(result.message)
            }
            is GeneralResult.GetUserSettings.Unauthorized -> {
                scene.showMessage(result.message)
            }
            is GeneralResult.GetUserSettings.SessionExpired -> {
                generalDataSource.submitRequest(GeneralRequest.Logout(true, false))
            }
            is GeneralResult.GetUserSettings.Forbidden -> {
                host.showConfirmPasswordDialog(uiObserver)
            }
            is GeneralResult.GetUserSettings.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
    }

    private fun onUpdateMailbox(result: GeneralResult.ActiveAccountUpdateMailbox){
        when(result){
            is GeneralResult.ActiveAccountUpdateMailbox.Success -> {
                handleActiveAccountSuccessfulMailboxUpdate(result)
            }
        }
    }

    private fun handleActiveAccountSuccessfulMailboxUpdate(resultData: GeneralResult.ActiveAccountUpdateMailbox.Success) {
        if (resultData.data != null) {
            resultData.data.parsedEvents.forEach {
                when(it.cmd){
                    Event.Cmd.peerUserChangeName -> {
                        val newName = (it as ParsedEvent.NameChange).newName
                        scene.updateProfileName(newName)
                    }
                    Event.Cmd.profilePictureChanged -> {
                        scene.updateAvatarByPeerEvent(activeAccount.name, activeAccount.userEmail)
                    }
                }
            }
        }
    }

    private fun onDeleteAccount(result: GeneralResult.DeleteAccount){
        scene.toggleGeneralDialogLoad(false)
        when(result) {
            is GeneralResult.DeleteAccount.Success -> {
                if(result.activeAccount == null)
                    host.goToScene(
                            params = SignInParams(),
                            activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.delete_account_toast_message)),
                            keep = false, deletePastIntents = true
                    )
                else {
                    activeAccount = result.activeAccount
                    host.goToScene(
                            params = MailboxParams(),
                            activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            keep = false,
                            deletePastIntents = true
                    )
                }
            }
            is GeneralResult.DeleteAccount.Failure -> {
                scene.setGeneralDialogWithInputError(result.message)
            }
        }
    }

    private fun onContactNameChanged(result: GeneralResult.ChangeContactName){
        when(result) {
            is GeneralResult.ChangeContactName.Success -> {
                scene.updateProfileName(result.fullName)
                activeAccount.updateFullName(storage, model.userData.name)
                scene.showMessage(UIMessage(R.string.profile_name_updated))
            }
            is GeneralResult.ChangeContactName.Failure -> {
                scene.updateProfileName(activeAccount.name)
                model.userData.name = activeAccount.name
                scene.showMessage(UIMessage(R.string.error_updating_account))
            }
        }
    }

    private fun onProfilePictureSet(result: ProfileResult.SetProfilePicture){
        when(result) {
            is ProfileResult.SetProfilePicture.Success -> {
                Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.recipientId}"))
                Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.domain}/${activeAccount.recipientId}"))
                scene.hideProfilePictureProgress()
                scene.showMessage(UIMessage(R.string.profile_picture_updated))
            }
            is ProfileResult.SetProfilePicture.Failure -> {
                scene.resetProfilePicture(model.userData.name)
                scene.hideProfilePictureProgress()
                scene.showMessage(result.message)
            }
            is ProfileResult.SetProfilePicture.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
    }

    private fun onProfilePictureDeleted(result: ProfileResult.DeleteProfilePicture){
        scene.hideProfilePictureProgress()
        when(result) {
            is ProfileResult.DeleteProfilePicture.Success -> {
                scene.resetProfilePicture(model.userData.name)
                scene.showMessage(UIMessage(R.string.profile_picture_deleted))
            }
            is ProfileResult.DeleteProfilePicture.Failure -> {
                scene.showMessage(result.message)
            }
            is ProfileResult.DeleteProfilePicture.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
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

    private fun setBitmapOnProfileImage(imagePath: String){
        val file = File(imagePath)
        val bitmapImage = Utility.getBitmapFromFile(file)

        if(bitmapImage != null) {
            scene.showProfilePictureProgress()
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
            dataSource.submitRequest(ProfileRequest.SetProfilePicture(rotatedBitmap))
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

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(uiObserver, activeAccount.userEmail, dialogType)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {
            host.runOnUiThread(Runnable {
                scene.dismissLinkDeviceDialog()
            })
        }

        override fun onSyncDeviceDismiss(accountEmail: String) {
            host.runOnUiThread(Runnable {
                scene.dismissSyncDeviceDialog()
            })
        }

        override fun onAccountSuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    showSuspendedAccountDialog()
            })
        }

        override fun onAccountUnsuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    scene.dismissAccountSuspendedDialog()
            })
        }

        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showSyncDeviceAuthConfirmation(trustedDeviceInfo)
            })
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

        override fun onNewEvent(recipientId: String, domain: String) {
            if(recipientId == activeAccount.recipientId && domain == activeAccount.domain) {
                host.runOnUiThread(Runnable {
                    generalDataSource.submitRequest(
                            GeneralRequest.ActiveAccountUpdateMailbox(Label.defaultItems.inbox,
                                    host.getLocalizedString(UIMessage(R.string.unable_to_decrypt)))
                    )
                })
            }
        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                host.showConfirmPasswordDialog(uiObserver)
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