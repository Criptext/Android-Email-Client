package com.email.scenes.composer

import android.content.DialogInterface
import com.email.BaseActivity
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
                            model.fullEmail?.email?.threadid, scene.getDataInputByUser()))
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
                dataSource.submitRequest(ComposerRequest.SaveEmailAsDraftAndSend(data))
        } else
            scene.showError(UIMessage(R.string.no_recipients_error))
    }

    override val menuResourceId
        get() = if (isReadyForSending()) R.menu.composer_menu_enabled
                              else R.menu.composer_menu_disabled

    override fun onStart(activityMessage: ActivityMessage?): Boolean {

        dataSourceController.setDataSourceListener()

        if(model.fullEmail != null) {
            val fullEmail = model.fullEmail!!
            when(model.composerType) {
                ComposerTypes.REPLY -> {
                    (host as BaseActivity).title = "REPLY"
                    model.body = fullEmail.email.content
                    model.to.add(fullEmail.from)
                }

                ComposerTypes.REPLY_ALL -> {
                    (host as BaseActivity).title = "REPLY ALL"
                    model.body = fullEmail.email.content
                    model.to.add(fullEmail.from)
                    model.to.addAll(fullEmail.to)
                    model.cc.addAll(fullEmail.cc)
                }

                ComposerTypes.FORWARD -> {
                    (host as BaseActivity).title = "FORWARD"
                    model.body = fullEmail.email.content
                }
            }
        }

        scene.bindWithModel(firstTime = model.firstTime, composerInputData = ComposerInputData.fromModel(model),
                defaultRecipients = model.defaultRecipients,
                replyData = if(model.fullEmail == null) null else ReplyData.FromModel(model))
        dataSourceController.getAllContacts()
        dataSource.listener = dataSourceListener
        scene.observer = observer
        model.firstTime = false

        return false
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
                    dataSource.submitRequest(ComposerRequest.SaveEmailAsDraft(scene.getDataInputByUser()))
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