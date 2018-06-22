package com.email.scenes.composer

import android.Manifest
import android.content.DialogInterface
import android.os.Build
import com.email.ExternalActivityParams
import com.email.IHostActivity
import com.email.R
import com.email.bgworker.BackgroundWorkManager
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.*
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.params.MailboxParams
import com.email.utils.KeyboardManager
import com.email.utils.UIMessage
import com.email.utils.virtuallist.VirtualList
import java.io.File
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat.checkSelfPermission
import com.email.BaseActivity


/**
 * Created by gabriel on 2/26/18.
 */

class ComposerController(private val model: ComposerModel,
                         private val scene: ComposerScene,
                         private val host: IHostActivity,
                         private val dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>)
    : SceneController() {

    private val dataSourceController = DataSourceController(dataSource)

    private val observer = object: ComposerUIObserver {
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
            if(host.checkAndRequestPermission(BaseActivity.RequestCode.readAccess.ordinal,
                            Manifest.permission.READ_EXTERNAL_STORAGE)){
                host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
            }
        }

        override fun onBackButtonClicked() {
            if(shouldGoBackWithoutSave()){
                host.finishScene()
            }
            else{
                showDraftDialog()
            }
        }
    }

    private val dataSourceListener: (ComposerResult) -> Unit = { result ->
        when(result) {
            is ComposerResult.GetAllContacts -> onContactsLoaded(result)
            is ComposerResult.SaveEmail -> onEmailSavesAsDraft(result)
            is ComposerResult.DeleteDraft -> host.finishScene()
            is ComposerResult.UploadFile -> onUploadFile(result)
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
                    host.finishScene()
                }
                else {
                    model.emailDetailActivity?.finishScene()
                    val sendMailMessage = ActivityMessage.SendMail(emailId = result.emailId,
                            threadId = result.threadId,
                            composerInputData = result.composerInputData,
                            attachments = result.attachments)
                    host.exitToScene(MailboxParams(), sendMailMessage)
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

    private fun updateModelWithInputData(data: ComposerInputData) {
        model.to.clear()
        model.to.addAll(data.to)
        model.cc.clear()
        model.cc.addAll(data.cc)
        model.bcc.clear()
        model.bcc.addAll(data.bcc)
        model.body = data.body
        model.subject = data.subject
    }

    private fun isReadyForSending() = model.to.isNotEmpty()

    private fun uploadSelectedFile(filepath: String){
        dataSource.submitRequest(ComposerRequest.UploadAttachment(filepath = filepath))
    }

    private fun onSendButtonClicked() {
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)

        if(isReadyForSending()) {
            val validationError = Validator.validateContacts(data)
            if (validationError != null)
                scene.showError(validationError.toUIMessage())
            else
                dataSource.submitRequest(ComposerRequest.SaveEmailAsDraft(
                        threadId =  model.fullEmail?.email?.threadId,
                        emailId = if(model.composerType == ComposerTypes.CONTINUE_DRAFT)
                            model.fullEmail?.email?.id else null,
                        composerInputData = data,
                        onlySave = false, attachments = model.attachments))
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
            addNewAttachments(activityMessage.filesMetadata)
            return true
        }
        return false
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {

        dataSourceController.setDataSourceListener()

        scene.bindWithModel(firstTime = model.firstTime,
                composerInputData = ComposerInputData.fromModel(model),
                defaultRecipients = model.defaultRecipients,
                replyData = if(model.fullEmail == null) null else ReplyData.FromModel(model),
                attachments = model.attachments)
        dataSourceController.getAllContacts()
        scene.observer = observer
        model.firstTime = false

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

        return if(shouldGoBackWithoutSave()) {
            true
        }
        else {
            showDraftDialog()
            false
        }
    }

    private fun shouldGoBackWithoutSave(): Boolean{
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)
        return !Validator.mailHasMoreThanSignature(data, "")
    }

    private fun showDraftDialog(){
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    dataSource.submitRequest(ComposerRequest.SaveEmailAsDraft(
                            threadId = model.fullEmail?.email?.threadId,
                            emailId = if(model.composerType == ComposerTypes.CONTINUE_DRAFT)
                                model.fullEmail?.email?.id else null,
                            composerInputData = scene.getDataInputByUser(),
                            onlySave = true, attachments = model.attachments))
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    if(model.composerType == ComposerTypes.CONTINUE_DRAFT){
                        dataSource.submitRequest(ComposerRequest.DeleteDraft(
                                emailId = model.fullEmail?.email?.id!!
                        ))
                    }
                    else {
                        host.finishScene()
                    }
                }
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

        fun clearDataSourceListener() {
            dataSource.listener = null
        }

        fun getAllContacts(){
            val req = ComposerRequest.GetAllContacts()
            dataSource.submitRequest(req)
        }

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.READ_EXTERNAL_STORAGE }
        if (indexOfPermission < 0) {
            return
        }
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED){
            scene.showError(UIMessage(R.string.permission_filepicker_rationale))
            return
        }
        host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
    }
}