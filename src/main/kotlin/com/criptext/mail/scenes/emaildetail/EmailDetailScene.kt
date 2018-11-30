package com.criptext.mail.scenes.emaildetail

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
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
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.ui.SnackBarHelper
import com.criptext.mail.utils.uiobserver.UIObserver
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
    fun showMessage(message : UIMessage)
    fun notifyFullEmailListChanged()
    fun notifyFullEmailChanged(position: Int)
    fun notifyLabelsChanged(updatedLabels: List<Label>)
    fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener)
    fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener)
    fun showDialogDeleteEmail(onDeleteEmailListener: OnDeleteEmailListener, fullEmail: FullEmail)
    fun onFetchedSelectedLabels(
            selectedLabels: List<Label>,
            allLabels: List<Label>)

    fun onDecryptedBody(decryptedText: String)
    fun updateAttachmentProgress(emailPosition: Int, attachmentPosition: Int)
    fun dismissConfirmPasswordDialog()
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun showStartGuideEmailIsRead(view: View)
    fun showStartGuideMenu(view: View)

    class EmailDetailSceneView(
            private val emailDetailView: View,
            private val hostActivity: IHostActivity)
        : EmailDetailScene {

        private val context = emailDetailView.context

        private var fullEmailsRecyclerView: FullEmailRecyclerView? = null

        private val labelChooserDialog = LabelChooserDialog(context, emailDetailView)
        private val moveToDialog = MoveToDialog(context)
        private val deleteThreadDialog = DeleteThreadDialog(context)
        private val deleteEmailDialog = DeleteEmailDialog(context)
        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)

        private val recyclerView: RecyclerView by lazy {
            emailDetailView.findViewById<RecyclerView>(R.id.emails_detail_recycler)
        }

        private val backButton: ImageView by lazy {
            emailDetailView.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        override var observer: EmailDetailUIObserver? = null

        override fun attachView(
                fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
                fullEmailList : VirtualList<FullEmail>, fileDetailList: Map<Long, List<FileDetail>>,
                observer: EmailDetailUIObserver){

            this.observer = observer
            val isStarred = EmailThreadValidator.isLabelInList(fullEmailList[0].labels, Label.LABEL_STARRED)

            fullEmailsRecyclerView = FullEmailRecyclerView(
                    recyclerView,
                    fullEmailEventListener,
                    fullEmailList,
                    fileDetailList,
                    getLabelsFromEmails(fullEmailList),
                    isStarred
                    )

            fullEmailsRecyclerView?.scrollToLast()

            backButton.setOnClickListener {
                observer.onBackButtonPressed()
            }
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

        private fun labelsToVirtualList(labels: List<Label>)
                : VirtualList<Label>{
            val labelsList = ArrayList(labels).filter { it.type != LabelTypes.SYSTEM}
            return VirtualList.Map(labelsList, { t->t })
        }

        override fun notifyFullEmailListChanged() {
            fullEmailsRecyclerView?.notifyFullEmailListChanged()
        }

        override fun notifyFullEmailChanged(position: Int) {
            fullEmailsRecyclerView?.notifyFullEmailChanged(position = position)
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
                    currentFolder = Label.LABEL_ALL_MAIL)
        }

        override fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener) {
            deleteThreadDialog.showDeleteThreadDialog(onDeleteThreadListener)
        }

        override fun showDialogDeleteEmail(onDeleteEmailListener: OnDeleteEmailListener, fullEmail: FullEmail) {
            deleteEmailDialog.showDeleteEmailDialog(onDeleteEmailListener, fullEmail)
        }

        override fun onDecryptedBody(decryptedText: String) {
            
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun notifyLabelsChanged(updatedLabels: List<Label>) {
            fullEmailsRecyclerView?.notifyLabelsChanged(labelsToVirtualList(updatedLabels),
                    updatedLabels.contains(Label.defaultItems.starred))
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

        override fun showMessage(message: UIMessage) {
            SnackBarHelper.show(emailDetailView, context.getLocalizedUIMessage(message))
        }

        override fun showStartGuideEmailIsRead(view: View) {
            observer?.showStartGuideEmailIsRead(view)
        }

        override fun showStartGuideMenu(view: View){
            observer?.showStartGuideMenu(view)
        }
    }

}
