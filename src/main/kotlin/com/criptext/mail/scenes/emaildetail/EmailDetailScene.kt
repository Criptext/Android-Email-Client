package com.criptext.mail.scenes.emaildetail

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.SecureEmail
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.ui.EmailDetailUIObserver
import com.criptext.mail.scenes.label_chooser.LabelChooserDialog
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.scenes.emaildetail.ui.FullEmailRecyclerView
import com.criptext.mail.scenes.emaildetail.ui.holders.FullEmailHolder
import com.criptext.mail.scenes.emaildetail.ui.labels.LabelsRecyclerView
import com.criptext.mail.scenes.label_chooser.LabelDataHandler
import com.criptext.mail.scenes.mailbox.*
import com.criptext.mail.utils.EmailThreadValidator
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailScene {

    var observer: EmailDetailUIObserver?
    fun attachView(
            fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
            fullEmailList : VirtualList<FullEmail>, fileDetailList: Map<Long, List<FileDetail>>,
            observer: EmailDetailUIObserver)

    fun showError(message : UIMessage)
    fun notifyFullEmailListChanged()
    fun notifyFullEmailChanged(position: Int)
    fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener)
    fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener)
    fun showDialogDeleteEmail(onDeleteEmailListener: OnDeleteEmailListener, fullEmail: FullEmail)
    fun onFetchedSelectedLabels(
            selectedLabels: List<Label>,
            allLabels: List<Label>)

    fun onDecryptedBody(decryptedText: String)
    fun updateAttachmentProgress(emailPosition: Int, attachmentPosition: Int)

    class EmailDetailSceneView(
            private val emailDetailView: View,
            private val hostActivity: IHostActivity)
        : EmailDetailScene {

        private val context = emailDetailView.context

        private lateinit var fullEmailsRecyclerView: FullEmailRecyclerView
        private lateinit var labelsRecyclerView: LabelsRecyclerView

        private val labelChooserDialog = LabelChooserDialog(context, emailDetailView)
        private val moveToDialog = MoveToDialog(context)
        private val deleteThreadDialog = DeleteThreadDialog(context)
        private val deleteEmailDialog = DeleteEmailDialog(context)

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

        private val starredImage: ImageView by lazy {
            emailDetailView.findViewById<ImageView>(R.id.starred)
        }

        override var observer: EmailDetailUIObserver? = null

        override fun attachView(
                fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
                fullEmailList : VirtualList<FullEmail>, fileDetailList: Map<Long, List<FileDetail>>,
                observer: EmailDetailUIObserver){

            this.observer = observer
            textViewSubject.text = if (fullEmailList[0].email.subject.isEmpty())
                textViewSubject.context.getString(R.string.nosubject)
            else fullEmailList[0].email.subject

            val isStarred = EmailThreadValidator.isLabelInList(fullEmailList[0].labels, SecureEmail.LABEL_STARRED)
            if(isStarred){
                setIconAndColor(R.drawable.starred, R.color.starred)
            }

            labelsRecyclerView = LabelsRecyclerView(recyclerLabelsView, getLabelsFromEmails(fullEmailList))

            fullEmailsRecyclerView = FullEmailRecyclerView(
                    recyclerView,
                    fullEmailEventListener,
                    fullEmailList,
                    fileDetailList
                    )

            fullEmailsRecyclerView.scrollToLast()

            backButton.setOnClickListener {
                observer.onBackButtonPressed()
            }

            starredImage.setOnClickListener({
                observer.onStarredButtonPressed(!isStarred)
            })

        }

        private fun setIconAndColor(drawable: Int, color: Int){
            Picasso.with(context).load(drawable).into(starredImage, object : Callback {
                override fun onError() {}
                override fun onSuccess() {
                    DrawableCompat.setTint(starredImage.drawable,
                            ContextCompat.getColor(context, color))
                }
            })
        }

        private fun getLabelsFromEmails(
                emails: VirtualList<FullEmail>) : VirtualList<Label> {
            val labelSet = HashSet<Label>()
            for (i in 0 until emails.size) {
                labelSet.addAll(emails[i].labels)
            }
            val labelsList = ArrayList(labelSet).filter { it.type != LabelTypes.SYSTEM}
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
                    currentFolder = SecureEmail.LABEL_ALL_MAIL)
        }

        override fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener) {
            deleteThreadDialog.showDeleteThreadDialog(onDeleteThreadListener)
        }

        override fun showDialogDeleteEmail(onDeleteEmailListener: OnDeleteEmailListener, fullEmail: FullEmail) {
            deleteEmailDialog.showDeleteEmailDialog(onDeleteEmailListener, fullEmail)
        }

        override fun onDecryptedBody(decryptedText: String) {
            
        }


        override fun updateAttachmentProgress(emailPosition: Int, attachmentPosition: Int) {
            val holder = recyclerView.findViewHolderForAdapterPosition(emailPosition)
                    as? FullEmailHolder ?: return
            holder.updateAttachmentProgress(attachmentPosition)
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
