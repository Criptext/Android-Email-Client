package com.email.scenes.composer

import android.content.DialogInterface
import com.email.ExternalActivityParams
import com.email.IHostActivity
import com.email.R
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.*
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.params.MailboxParams
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerController(private val model: ComposerModel,
                         private val scene: ComposerScene,
                         private val host: IHostActivity,
                         private val dataSource: ComposerDataSource) : SceneController() {

    private val dataSourceController = DataSourceController(dataSource)

    private val observer = object: ComposerUIObserver {
        override fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean) {
            scene.toggleExtraFieldsVisibility(visible = userIsEditingRecipients)
        }

        override fun onRecipientListChanged() {
            val data = scene.getDataInputByUser()
            updateModelWithInputData(data)
            host.refreshToolbarItems()
        }

        override fun onAttachmentButtonClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
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
        }
    }

    fun onEmailSavesAsDraft(result: ComposerResult.SaveEmail){
        when (result) {
            is ComposerResult.SaveEmail.Success -> {
                if(result.onlySave){
                    host.finishScene()
                }
                else {
                    host.exitToScene(MailboxParams(), ActivityMessage.SendMail(result.emailId,
                            model.fullEmail?.email?.threadId, scene.getDataInputByUser()))
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

    fun updateModelWithInputData(data: ComposerInputData) {
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

    private fun onSendButtonClicked() {
        if(isReadyForSending()) {
            val data = scene.getDataInputByUser()
            updateModelWithInputData(data)
            val validationError = Validator.validateContacts(data)
            if (validationError != null)
                scene.showError(validationError.toUIMessage())
            else
                dataSource.submitRequest(ComposerRequest.SaveEmailAsDraft(
                        threadId =  model.fullEmail?.email?.threadId,
                        emailId = if(model.composerType == ComposerTypes.CONTINUE_DRAFT)
                            model.fullEmail?.email?.id else null,
                        composerInputData = data,
                        onlySave = false))
        } else
            scene.showError(UIMessage(R.string.no_recipients_error))
    }

    override val menuResourceId
        get() = if (isReadyForSending()) R.menu.composer_menu_enabled
                              else R.menu.composer_menu_disabled

    private fun addNewAttachments(filepaths: List<String>) {
        filepaths.forEach {
            if (! model.attachments.contains(it))
                model.attachments[it] = ComposerAttachment(it)
        }
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        if (activityMessage is ActivityMessage.AddAttachments) {
            addNewAttachments(activityMessage.filepaths)
            return true
        }
        return false
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {

        dataSourceController.setDataSourceListener()

        scene.bindWithModel(firstTime = model.firstTime,
                composerInputData = ComposerInputData.fromModel(model),
                defaultRecipients = model.defaultRecipients,
                replyData = if(model.fullEmail == null) null else ReplyData.FromModel(model))
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
                            onlySave = true))
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    //TODO Delete draft if necessary
                    host.finishScene()
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
            private val dataSource: ComposerDataSource){

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

}