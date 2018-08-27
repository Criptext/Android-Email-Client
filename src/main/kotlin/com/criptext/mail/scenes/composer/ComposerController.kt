package com.criptext.mail.scenes.composer


import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.*
import com.criptext.mail.scenes.composer.ui.ComposerUIObserver
import com.criptext.mail.scenes.params.EmailDetailParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.remotechange.data.RemoteChangeRequest
import com.criptext.mail.utils.remotechange.data.RemoteChangeResult
import com.criptext.mail.validation.FormInputState


/**
 * Created by gabriel on 2/26/18.
 */

class ComposerController(private val model: ComposerModel,
                         private val scene: ComposerScene,
                         private val host: IHostActivity,
                         private val activeAccount: ActiveAccount,
                         private val remoteChangeDataSource: BackgroundWorkManager<RemoteChangeRequest, RemoteChangeResult>,
                         private val dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>)
    : SceneController() {

    val arePasswordsMatching: Boolean
        get() = model.passwordText == model.confirmPasswordText

    private val dataSourceController = DataSourceController(dataSource)

    private val observer = object: ComposerUIObserver {
        override fun onNewCamAttachmentRequested() {
            host.launchExternalActivityForResult(ExternalActivityParams.Camera())
        }

        override fun onNewFileAttachmentRequested() {
            host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
        }

        override fun onNewGalleryAttachmentRequested() {
            host.launchExternalActivityForResult(ExternalActivityParams.ImagePicker())
        }

        override fun sendDialogButtonPressed() {
            val data = scene.getDataInputByUser()
            updateModelWithInputData(data)

            if(isReadyForSending())
                saveEmailAsDraft(data, onlySave = false)
        }

        override fun sendDialogCancelPressed() {
            model.passwordText = ""
            model.confirmPasswordText = ""
            model.passwordState = FormInputState.Unknown()
        }

        override fun setOnCheckedChangeListener(isChecked: Boolean) {
            if(isChecked)
                checkPasswords(Pair(model.confirmPasswordText, model.passwordText))
            else {
                model.passwordForNonCriptextUsers = null
                scene.setPasswordForNonCriptextFromDialog(model.passwordForNonCriptextUsers)
            }
        }

        override fun onConfirmPasswordChangedListener(text: String) {
            model.confirmPasswordText = text
            model.passwordForNonCriptextUsers = text
            scene.setPasswordForNonCriptextFromDialog(model.passwordForNonCriptextUsers)
            checkPasswords(Pair(model.confirmPasswordText, model.passwordText))
        }

        override fun onPasswordChangedListener(text: String) {
            model.passwordText = text
            if(model.confirmPasswordText.isNotEmpty())
               checkPasswords(Pair(model.passwordText, model.confirmPasswordText))
        }

        override fun onAttachmentRemoveClicked(position: Int) {
            model.attachments.removeAt(position)
            scene.notifyAttachmentSetChanged()
        }

        override fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean) {
            scene.toggleExtraFieldsVisibility(visible = userIsEditingRecipients)
        }

        override fun onRecipientListChanged() {
            val data = scene.getDataInputByUser()
            updateModelWithInputData(data)
            host.refreshToolbarItems()
        }

        override fun onAttachmentButtonClicked() {
            if(host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                scene.showAttachmentsBottomDialog(this)
            }
        }

        override fun onBackButtonClicked() {
            if(shouldGoBackWithoutSave()){
                exitToEmailDetailScene()
            }
            else{
                showDraftDialog()
            }
        }

        override fun onOkButtonPressed(password: String) {
            remoteChangeDataSource.submitRequest(RemoteChangeRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            remoteChangeDataSource.submitRequest(RemoteChangeRequest.DeviceRemoved())
        }
    }

    private fun checkPasswords(passwords: Pair<String, String>) {
        if (arePasswordsMatching && passwords.first.length >= minimumPasswordLength) {
            scene.setPasswordError(null)
            scene.togglePasswordSuccess(show = true)
            model.passwordState = FormInputState.Valid()
            if (model.passwordState is FormInputState.Valid)
                scene.enableSendButtonOnDialog()
        } else if (arePasswordsMatching && passwords.first.isEmpty()) {
            scene.setPasswordError(null)
            scene.togglePasswordSuccess(show = false)
            model.passwordState = FormInputState.Unknown()
            scene.disableSendButtonOnDialog()
        } else if (arePasswordsMatching && passwords.first.length < minimumPasswordLength) {
            scene.togglePasswordSuccess(show = false)
            val errorMessage = UIMessage(R.string.pin_length_error)
            model.passwordState = FormInputState.Error(errorMessage)
            scene.setPasswordError(errorMessage)
            scene.disableSendButtonOnDialog()
        } else {
            val errorMessage = UIMessage(R.string.pin_mismatch_error)
            model.passwordState = FormInputState.Error(errorMessage)
            scene.setPasswordError(errorMessage)
            scene.togglePasswordSuccess(show = false)
            scene.disableSendButtonOnDialog()
        }
    }

    private val remoteChangeDataSourceListener: (RemoteChangeResult) -> Unit = { result ->
        when(result) {
            is RemoteChangeResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is RemoteChangeResult.ConfirmPassword -> onPasswordChangedRemotely(result)
        }
    }

    private val dataSourceListener: (ComposerResult) -> Unit = { result ->
        when(result) {
            is ComposerResult.GetAllContacts -> onContactsLoaded(result)
            is ComposerResult.SaveEmail -> onEmailSavesAsDraft(result)
            is ComposerResult.DeleteDraft -> exitToEmailDetailScene()
            is ComposerResult.UploadFile -> onUploadFile(result)
            is ComposerResult.LoadInitialData -> onLoadedInitialData(result)
        }
    }

    private fun onLoadedInitialData(result: ComposerResult.LoadInitialData) {
        when (result) {
            is ComposerResult.LoadInitialData.Success -> {
                updateModelWithInputData(result.initialData)
                bindWithModel(result.initialData, activeAccount.signature)
                model.initialized = true
            }

            is ComposerResult.LoadInitialData.Failure -> {
                scene.showError(result.message)
            }
        }
    }

    private fun onUploadFile(result: ComposerResult.UploadFile){
        when (result) {
            is ComposerResult.UploadFile.Register -> {
                val composerAttachment = getAttachmentByPath(result.filepath) ?: return
                composerAttachment.filetoken = result.filetoken
            }
            is ComposerResult.UploadFile.Progress -> {
                val composerAttachment = getAttachmentByPath(result.filepath) ?: return
                composerAttachment.uploadProgress = result.percentage
            }
            is ComposerResult.UploadFile.Success -> {
                val composerAttachment = getAttachmentByPath(result.filepath)
                composerAttachment?.uploadProgress = 100
                handleNextUpload()
            }
            is ComposerResult.UploadFile.Failure -> {
                removeAttachmentByPath(result.filepath)
                scene.showAttachmentErrorDialog(result.filepath)
                handleNextUpload()
            }
            is ComposerResult.UploadFile.Unauthorized -> {
                remoteChangeDataSource.submitRequest(RemoteChangeRequest.DeviceRemoved())
            }
            is ComposerResult.UploadFile.Forbidden -> {
                scene.showConfirmPasswordDialog(observer)
            }
        }
        scene.notifyAttachmentSetChanged()
    }

    private fun getAttachmentByPath(filepath: String): ComposerAttachment? {
        return model.attachments.firstOrNull{it.filepath == filepath}
    }

    private fun removeAttachmentByPath(filepath: String) {
        model.attachments.removeAll{it.filepath == filepath}
    }

    private fun onEmailSavesAsDraft(result: ComposerResult.SaveEmail) {
        when (result) {
            is ComposerResult.SaveEmail.Success -> {
                if(result.onlySave) {
                    host.exitToScene(MailboxParams(), ActivityMessage.DraftSaved(), false)
                }
                else {
                    val sendMailMessage = ActivityMessage.SendMail(emailId = result.emailId,
                            threadId = result.threadId,
                            composerInputData = result.composerInputData,
                            attachments = result.attachments, fileKey = model.fileKey)
                    host.exitToScene(MailboxParams(), sendMailMessage, false)
                }
            }
            is ComposerResult.SaveEmail.Failure -> {
                scene.showError(UIMessage(R.string.error_saving_as_draft))
            }
        }
    }

    private fun onContactsLoaded(result: ComposerResult.GetAllContacts){
        when (result) {
            is ComposerResult.GetAllContacts.Success -> {
                scene.setContactSuggestionList(result.contacts.toTypedArray())
            }
            is ComposerResult.GetAllContacts.Failure -> {
                scene.showError(UIMessage(R.string.error_getting_contacts))
            }
        }
    }

    private fun onDeviceRemovedRemotely(result: RemoteChangeResult.DeviceRemoved){
        when (result) {
            is RemoteChangeResult.DeviceRemoved.Success -> {
                host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)), true, true)
            }
        }
    }

    private fun onPasswordChangedRemotely(result: RemoteChangeResult.ConfirmPassword){
        when (result) {
            is RemoteChangeResult.ConfirmPassword.Success -> {
                scene.dismissConfirmPasswordDialog()
                scene.showMessage(UIMessage(R.string.update_password_success))
            }
            is RemoteChangeResult.ConfirmPassword.Failure -> {
                scene.setConfirmPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun updateModelWithInputData(data: ComposerInputData) {
        model.to.clear()
        model.to.addAll(data.to.map { Contact(it.id, it.email.decapitalize(), it.name) })
        model.cc.clear()
        model.cc.addAll(data.cc.map { Contact(it.id, it.email.decapitalize(), it.name) })
        model.bcc.clear()
        model.bcc.addAll(data.bcc.map { Contact(it.id, it.email.decapitalize(), it.name) })
        model.body = data.body
        model.subject = data.subject
    }

    private fun isReadyForSending() = model.to.isNotEmpty()

    private fun uploadSelectedFile(filepath: String){
        dataSource.submitRequest(ComposerRequest.UploadAttachment(filepath = filepath, fileKey = model.fileKey))
    }

    private fun saveEmailAsDraft(composerInputData: ComposerInputData, onlySave: Boolean) {
        val draftId = when (model.type) {
            is ComposerType.Draft -> model.type.draftId
            else -> null
        }
        val threadPreview =  when (model.type) {
            is ComposerType.Reply -> model.type.threadPreview
            is ComposerType.ReplyAll -> model.type.threadPreview
            is ComposerType.Forward -> model.type.threadPreview
            is ComposerType.Draft -> model.type.threadPreview
            else -> null
        }
        dataSource.submitRequest(ComposerRequest.SaveEmailAsDraft(
                threadId = threadPreview?.threadId,
                emailId = draftId,
                composerInputData = composerInputData,
                onlySave = onlySave, attachments = model.attachments, fileKey = model.fileKey))

    }

    private fun onSendButtonClicked() {
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)

        if(isReadyForSending()) {
            val validationError = Validator.validateContacts(data)
            if (validationError != null)
                scene.showError(validationError.toUIMessage())
            else
                if(Validator.criptextOnlyContacts(data))
                    saveEmailAsDraft(data, onlySave = false)
                else
                    scene.showNonCriptextEmailSendDialog(observer)
        } else
            scene.showError(UIMessage(R.string.no_recipients_error))
    }

    override val menuResourceId
        get() = if (isReadyForSending()) R.menu.composer_menu_enabled
                              else R.menu.composer_menu_disabled

    private fun addNewAttachments(filesMetadata: List<Pair<String, Long>>) {
        val isNewAttachment: (Pair<String, Long>) -> (Boolean) = { data ->
            model.attachments.indexOfFirst { it.filepath == data.first  } < 0
        }
        model.attachments.addAll(filesMetadata.filter(isNewAttachment).map {
            ComposerAttachment(it.first, it.second)
        })
        scene.notifyAttachmentSetChanged()
        handleNextUpload()
    }

    private fun handleNextUpload(){
        if(model.attachments.indexOfFirst { it.uploadProgress in 0..99 } >= 0){
            return
        }
        val attachmentToUpload = model.attachments.firstOrNull { it.uploadProgress == -1 } ?: return
        uploadSelectedFile(attachmentToUpload.filepath)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        if (activityMessage is ActivityMessage.AddAttachments) {
            generateEmailFileKey()
            addNewAttachments(activityMessage.filesMetadata)
            return true
        }
        return false
    }

    private fun generateEmailFileKey(){
        if(model.fileKey != null)  return
        model.fileKey = if(model.type is ComposerType.Empty) {
            val aesKey = AESUtil.generateSecureRandomBytes()
            val aesIV = AESUtil.generateSecureRandomBytes()

            aesKey.plus(":".plus(aesIV))
        } else null
    }

    private fun loadInitialData() {
        val type = model.type
        val request = when (type) {
            is ComposerType.Reply -> ComposerRequest.LoadInitialData(type, type.originalId)
            is ComposerType.ReplyAll -> ComposerRequest.LoadInitialData(type, type.originalId)
            is ComposerType.Forward -> ComposerRequest.LoadInitialData(type, type.originalId)
            is ComposerType.Draft -> ComposerRequest.LoadInitialData(type, type.draftId)
            is ComposerType.Support -> ComposerRequest.LoadInitialData(type, 0)
            else -> null
        }

        if (request != null) dataSource.submitRequest(request)
    }

    private fun bindWithModel(composerInputData: ComposerInputData, signature: String) {
        if(model.isReplyOrDraft || model.isSupport){
            scene.setFocusToComposer()
        }
        scene.bindWithModel(firstTime = model.firstTime,
                composerInputData = composerInputData,
                attachments = model.attachments,
                signature = signature)
        model.firstTime = false
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {

        dataSourceController.setDataSourceListener()
        remoteChangeDataSource.listener = remoteChangeDataSourceListener

        if (model.initialized)
            bindWithModel(ComposerInputData.fromModel(model), activeAccount.signature)
        else
            loadInitialData()

        dataSourceController.getAllContacts()
        scene.observer = observer

        return handleActivityMessage(activityMessage)
    }

    override fun onStop() {
        // save state
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)

        scene.observer = null
        dataSource.listener = null
    }

    override fun onBackPressed(): Boolean {

        if(shouldGoBackWithoutSave()) {
            exitToEmailDetailScene()
        }
        else {
            showDraftDialog()

        }

        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    private fun exitToEmailDetailScene(){
        val threadPreview =  when (model.type) {
            is ComposerType.Reply -> model.type.threadPreview
            is ComposerType.ReplyAll -> model.type.threadPreview
            is ComposerType.Forward -> model.type.threadPreview
            is ComposerType.Draft -> model.type.threadPreview
            else -> null
        }
        val currentLabel = when (model.type) {
            is ComposerType.Reply -> model.type.currentLabel
            is ComposerType.ReplyAll -> model.type.currentLabel
            is ComposerType.Forward -> model.type.currentLabel
            is ComposerType.Draft -> model.type.currentLabel
            else -> null
        }
        if(model.type is ComposerType.Empty || threadPreview == null || currentLabel == null){
            return host.finishScene()
        }
        val params = EmailDetailParams(threadId = threadPreview.threadId,
                currentLabel = currentLabel, threadPreview = threadPreview)
        host.exitToScene(params, null, true)
    }

    private fun shouldGoBackWithoutSave(): Boolean{
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)
        return !Validator.mailHasMoreThanSignature(data, activeAccount.signature)
    }

    private fun exitDeletingDraft() {
        val draftType = model.type as? ComposerType.Draft
        if (draftType != null)
            dataSource.submitRequest(ComposerRequest.DeleteDraft(draftType.draftId))
        else
            exitToEmailDetailScene()
    }

    private fun showDraftDialog(){
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE ->
                    saveEmailAsDraft(composerInputData = scene.getDataInputByUser(), onlySave = true)

                DialogInterface.BUTTON_NEGATIVE ->
                    exitDeletingDraft()
            }
        }

        scene.showDraftDialog(dialogClickListener)
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.composer_send -> onSendButtonClicked()
        }
    }

    private inner class DataSourceController(
            private val dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>){

        fun setDataSourceListener() {
            dataSource.listener = dataSourceListener
        }

        fun getAllContacts(){
            val req = ComposerRequest.GetAllContacts()
            dataSource.submitRequest(req)
        }

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != BaseActivity.RequestCode.writeAccess.ordinal) return

        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
        if (indexOfPermission < 0) return
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED){
            scene.showError(UIMessage(R.string.permission_filepicker_rationale))
            return
        }
        host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
    }

    companion object {
        val minimumPasswordLength = 3
    }
}