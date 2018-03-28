package com.email.scenes.composer

import com.email.BaseActivity
import com.email.db.models.Contact
import com.email.IHostActivity
import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerDataSource
import com.email.scenes.composer.data.ComposerRequest
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.composer.data.ComposerTypes
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.composer.ui.UIData
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerController(private val model: ComposerModel,
                         private val scene: ComposerScene,
                         private val host: IHostActivity,
                         private val dataSource: ComposerDataSource) : SceneController() {

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
    }


    private val dataSourceListener: (ComposerResult) -> Unit = { result ->
        when(result) {
            is ComposerResult.SendMail -> onSendMailFinished(result)
        }
    }

    private fun onSendMailFinished(result: ComposerResult.SendMail) {
        when (result) {
            is ComposerResult.SendMail.Success -> host.finishScene()
            is ComposerResult.SendMail.Failure -> scene.showError(result.message)
        }
    }

    private fun updateModelWithInputData(data: UIData) {
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
                dataSource.submitRequest(ComposerRequest.SendMail(data))
        } else
            scene.showError(UIMessage(R.string.no_recipients_error))
    }

    override val menuResourceId
        get() = if (isReadyForSending()) R.menu.composer_menu_enabled
                              else R.menu.composer_menu_disabled

    override fun onStart() {
        if(model.fullEmail != null) {
            val fullEmail = model.fullEmail!!
            when(model.composerType) {
                ComposerTypes.REPLY -> {
                    (host as BaseActivity).title = "REPLY"
                    model.body = fullEmail.email.content
                    if(fullEmail.from != null)
                        model.to.add(fullEmail.from)
                        model.cc.addAll(fullEmail.cc)
                }

                ComposerTypes.REPLY_ALL -> {
                    (host as BaseActivity).title = "REPLY ALL"
                    model.to.addAll(fullEmail.cc)
                    model.cc.addAll(fullEmail.cc)
                }

                ComposerTypes.FORWARD -> {
                    (host as BaseActivity).title = "FORWARD"
                    model.body = fullEmail.email.content
                }
            }
        }

        scene.bindWithModel(firstTime = model.firstTime, uiData = UIData.fromModel(model),
                defaultRecipients = model.defaultRecipients)
        scene.setContactSuggestionList(arrayOf(
                Contact("gianni@criptext.com", "Gianni Carlo"),
                Contact("mayer@criptext.com", "Mayer Mizrachi")))
        dataSource.listener = dataSourceListener
        scene.observer = observer
        model.firstTime = false
    }

    override fun onStop() {
        // save state
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)

        scene.observer = null
        dataSource.listener = null
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.composer_send -> onSendButtonClicked()
        }
    }

}