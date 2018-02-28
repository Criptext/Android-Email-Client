package com.email.scenes.composer

import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerDataSource
import com.email.scenes.composer.data.ComposerRequest
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.composer.ui.UIData
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerController(private val model: ComposerModel,
                         private val scene: ComposerScene,
                         private val dataSource: ComposerDataSource) : SceneController() {

    private val observer = object: ComposerUIObserver {
        override fun onRecipientListChanged() {
            val data = scene.getDataInputByUser()
            updateModelWithInputData(data)
        }

        override fun onAttachmentButtonClicked() {
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

    override val menuResourceId = null

    override fun onStart() {
        scene.bindWithModel(UIData.fromModel(model))
        scene.observer = observer
    }

    override fun onStop() {
        // save state
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)

        scene.observer = null
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