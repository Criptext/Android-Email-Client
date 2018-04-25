package com.email.scenes.composer.mocks

import android.content.DialogInterface
import android.support.v4.app.DialogFragment
import com.email.db.models.Contact
import com.email.scenes.composer.AddressError
import com.email.scenes.composer.ComposerScene
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.composer.data.ReplyData
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.utils.UIMessage

/**
 * Created by danieltigse on 4/19/18.
 */

class MockedComposerScene: ComposerScene {

    var showedError: Boolean = false

    override var observer: ComposerUIObserver?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>, composerInputData: ComposerInputData, replyData: ReplyData?) {

    }

    override fun getDataInputByUser(): ComposerInputData {
        return ComposerInputData(to = arrayListOf(Contact(1, "gianni@jigl.com", "Giannni")),
                cc = ArrayList(), bcc = ArrayList(), subject = "Test",
                body = "<p>Hi</p>")
    }

    override fun showError(message: UIMessage) {
        showedError = true
    }

    override fun setContactSuggestionList(contacts: Array<Contact>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggleExtraFieldsVisibility(visible: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showDraftDialog(dialogClickListener: DialogInterface.OnClickListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}