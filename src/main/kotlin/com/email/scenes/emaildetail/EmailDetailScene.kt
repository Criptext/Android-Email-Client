package com.email.scenes.emaildetail

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.email.IHostActivity
import com.email.R
import com.email.db.MailFolders
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.label_chooser.LabelChooserDialog
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.emaildetail.ui.FullEmailRecyclerView
import com.email.scenes.emaildetail.ui.labels.LabelsRecyclerView
import com.email.scenes.label_chooser.LabelDataHandler
import com.email.scenes.mailbox.DeleteThreadDialog
import com.email.scenes.mailbox.MoveToDialog
import com.email.scenes.mailbox.OnDeleteThreadListener
import com.email.scenes.mailbox.OnMoveThreadsListener
import com.email.utils.virtuallist.VirtualList
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailScene {

    fun attachView(
            fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
            fullEmailList : VirtualList<FullEmail>)

    fun showError(message : UIMessage)
    fun notifyFullEmailListChanged()
    fun notifyFullEmailChanged(position: Int)
    fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener)
    fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener)
    fun onFetchedSelectedLabels(
            selectedLabels: List<Label>,
            allLabels: List<Label>)

    fun onDecryptedBody(decryptedText: String)

    class EmailDetailSceneView(
            private val emailDetailView: View,
            private val hostActivity: IHostActivity)
        : EmailDetailScene {

        private val context = emailDetailView.context

        private lateinit var fullEmailsRecyclerView: FullEmailRecyclerView
        private lateinit var labelsRecyclerView: LabelsRecyclerView

        private val labelChooserDialog = LabelChooserDialog(context)
        private val moveToDialog = MoveToDialog(context)
        private val deleteDialog = DeleteThreadDialog(context)

        private val recyclerView: RecyclerView by lazy {
            emailDetailView.findViewById<RecyclerView>(R.id.emails_detail_recycler)
        }

        private val recyclerLabelsView: RecyclerView by lazy {
            emailDetailView.findViewById<RecyclerView>(R.id.labels_recycler)
        }

        private val backButton: ImageView by lazy {
            emailDetailView.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewSubject: TextView by lazy {
            emailDetailView.findViewById<TextView>(R.id.textViewSubject)
        }

        override fun attachView(
                fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
                fullEmailList : VirtualList<FullEmail>){

            textViewSubject.text = if (fullEmailList[0].email.subject.isEmpty())
                textViewSubject.context.getString(R.string.nosubject)
            else fullEmailList[0].email.subject

            labelsRecyclerView = LabelsRecyclerView(recyclerLabelsView, getLabelsFromEmails(fullEmailList))

            fullEmailsRecyclerView = FullEmailRecyclerView(
                    recyclerView,
                    fullEmailEventListener,
                    fullEmailList)

            backButton.setOnClickListener {
                hostActivity.finishScene()
            }

        }

        private fun getLabelsFromEmails(
                emails: VirtualList<FullEmail>) : VirtualList<Label> {
            val labelSet = HashSet<Label>()
            for (i in 0 until emails.size) {
                labelSet.addAll(emails[i].labels)
            }
            val labelsList = ArrayList(labelSet)
            return VirtualList.Map(labelsList, { t->t })
        }

        override fun notifyFullEmailListChanged() {
            fullEmailsRecyclerView.notifyFullEmailListChanged()
        }

        override fun notifyFullEmailChanged(position: Int) {
            fullEmailsRecyclerView.notifyFullEmailChanged(position = position)
        }

        override fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler) {
            labelChooserDialog.showDialogLabelsChooser(dataHandler = labelDataHandler)
        }

        override fun onFetchedSelectedLabels(selectedLabels: List<Label>, allLabels: List<Label>) {
            labelChooserDialog.onFetchedLabels(
                    defaultSelectedLabels = selectedLabels,
                    allLabels = allLabels)
        }

        override fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener) {
            moveToDialog.showMoveToDialog(
                    onMoveThreadsListener = onMoveThreadsListener,
                    currentFolder = MailFolders.ALL_MAIL)
        }

        override fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener) {
            deleteDialog.showDeleteThreadDialog(onDeleteThreadListener)
        }

        override fun onDecryptedBody(decryptedText: String) {
        }

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }

}
