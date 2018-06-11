package com.email.scenes.emailDetail.mocks

import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.emaildetail.EmailDetailScene
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.label_chooser.LabelDataHandler
import com.email.scenes.mailbox.OnMoveThreadsListener
import com.email.utils.UIMessage
import com.email.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/29/18.
 */

class MockedEmailDetailView: EmailDetailScene {
    override fun showError(message: UIMessage) {
    }

    override fun onDecryptedBody(decryptedText: String) {
    }

    var notifiedDataSetChanged = false
    override fun attachView(
            fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
            fullEmailList: VirtualList<FullEmail>) {
        notifyFullEmailListChanged()
    }

    override fun notifyFullEmailListChanged() {
        notifiedDataSetChanged = true
    }

    override fun notifyFullEmailChanged(position: Int) {
    }

    override fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler) {
    }

    override fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener) {
    }

    override fun onFetchedSelectedLabels(selectedLabels: List<Label>, allLabels: List<Label>) {
    }

}