package com.criptext.mail.scenes.emaildetail


import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.ui.EmailDetailUIObserver
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.scenes.emaildetail.ui.FullEmailRecyclerView
import com.criptext.mail.scenes.emaildetail.ui.holders.FullEmailHolder
import com.criptext.mail.scenes.label_chooser.LabelChooserDialog
import com.criptext.mail.scenes.label_chooser.LabelDataHandler
import com.criptext.mail.scenes.mailbox.*
import com.criptext.mail.utils.*
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailScene {

    var observer: EmailDetailUIObserver?
    fun attachView(
            fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener,
            fullEmailList : VirtualList<FullEmail>, fileDetailList: Map<Long, List<FileDetail>>,
            observer: EmailDetailUIObserver, shouldOpenExpanded: Boolean, activeAccount: ActiveAccount)
    fun showError(message : UIMessage)
    fun showMessage(message : UIMessage, showAction: Boolean = false)
    fun notifyFullEmailListChanged()
    fun notifyFullEmailChanged(position: Int)
    fun notifyFullEmailRemoved(position: Int)
    fun notifyLabelsChanged(updatedLabels: List<Label>)
    fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener, currentFolder: String)
    fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener)
    fun showDialogDeleteEmail(onDeleteEmailListener: OnDeleteEmailListener, fullEmail: FullEmail)
    fun onFetchedSelectedLabels(
            selectedLabels: List<Label>,
            allLabels: List<Label>)

    fun onDecryptedBody(decryptedText: String)
    fun updateAttachmentProgress(emailPosition: Int, attachmentPosition: Int)
    fun updateInlineImage(emailPosition: Int, cid: String, filePath: String)
    fun removeAttachmentAt(emailPosition: Int, attachmentPosition: Int)
    fun dismissConfirmPasswordDialog()
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showStartGuideEmailIsRead(view: View)
    fun showStartGuideMenu(view: View)
    fun printFullEmail(info: HTMLUtils.PrintHeaderInfo, content: String, documentName: String,
                       isForward: Boolean)
    fun printAllFullEmail(info: List<HTMLUtils.PrintHeaderInfo>, content: List<String>, documentName: String,
                          isForward: Boolean)
    fun expandAllThread()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()

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
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

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
                observer: EmailDetailUIObserver, shouldOpenExpanded: Boolean, activeAccount: ActiveAccount){

            this.observer = observer
            val isStarred = EmailThreadValidator.isLabelInList(fullEmailList[0].labels, Label.LABEL_STARRED)

            fullEmailsRecyclerView = FullEmailRecyclerView(
                    recyclerView,
                    fullEmailEventListener,
                    fullEmailList,
                    fileDetailList,
                    getLabelsFromEmails(fullEmailList),
                    isStarred,
                    shouldOpenExpanded,
                    activeAccount
                    )

            fullEmailsRecyclerView?.scrollToLast()

            backButton.setOnClickListener {
                observer.onBackButtonPressed()
            }
        }

        private fun getLabelsFromEmails(
                emails: VirtualList<FullEmail>) : VirtualList<Label> {
            val labels = mutableListOf<Label>()
            emails.forEach { email ->
                email.labels.forEach {
                    if(!labels.contains(it)) labels.add(it)
                }
            }
            val labelsList = ArrayList(labels).filter { it.type != LabelTypes.SYSTEM}
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

        override fun notifyFullEmailRemoved(position: Int) {
            fullEmailsRecyclerView?.notifyFullEmailRemoved(position = position)
        }

        override fun showDialogLabelsChooser(labelDataHandler: LabelDataHandler) {
            labelChooserDialog.showDialogLabelsChooser(dataHandler = labelDataHandler)
        }

        override fun onFetchedSelectedLabels(selectedLabels: List<Label>, allLabels: List<Label>) {
            labelChooserDialog.onFetchedLabels(
                    defaultSelectedLabels = selectedLabels,
                    allLabels = allLabels)
        }

        override fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener,
                                      currentFolder: String) {
            moveToDialog.showMoveToDialog(
                    onMoveThreadsListener = onMoveThreadsListener,
                    currentFolder = currentFolder)
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

        override fun expandAllThread() {
            fullEmailsRecyclerView?.expandAndNotify()
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(observer, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(observer, trustedDeviceInfo)
        }

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
        }

        override fun setConfirmPasswordError(message: UIMessage) {

        }

        override fun printFullEmail(info: HTMLUtils.PrintHeaderInfo, content: String, documentName: String,
                                    isForward: Boolean) {
            val newWebView = WebView(context)
            newWebView.loadDataWithBaseURL("", HTMLUtils.
                    headerForPrinting(htmlText = content,
                            printData = info,
                            to = context.getLocalizedUIMessage(UIMessage(R.string.to)),
                            at = context.getLocalizedUIMessage(UIMessage(R.string.mail_template_at)),
                            message = context.getLocalizedUIMessage(UIMessage(R.string.message)),
                            isForward = isForward),
                    "text/html", "utf-8", "")
            PrintUtils.createWebPrintJob(
                    webView = newWebView,
                    documentName = documentName,
                    context = context
            )
        }

        override fun printAllFullEmail(info: List<HTMLUtils.PrintHeaderInfo>, content: List<String>,
                                       documentName: String, isForward: Boolean) {
            val newWebView = WebView(context)
            newWebView.loadDataWithBaseURL("", HTMLUtils.
                    headerForPrintingAll(htmlText = content,
                            printData = info,
                            to = context.getLocalizedUIMessage(UIMessage(R.string.to)),
                            at = context.getLocalizedUIMessage(UIMessage(R.string.mail_template_at)),
                            message = context.getLocalizedUIMessage(UIMessage(R.string.messages)),
                            isForward = isForward),
                    "text/html", "utf-8", "")
            PrintUtils.createWebPrintJob(
                    webView = newWebView,
                    documentName = documentName,
                    context = context
            )
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

        override fun updateInlineImage(emailPosition: Int, cid: String, filePath: String) {
            val holder = recyclerView.findViewHolderForAdapterPosition(emailPosition)
                    as? FullEmailHolder ?: return
            holder.updateInlineImage(cid, filePath)
        }

        override fun removeAttachmentAt(emailPosition: Int, attachmentPosition: Int) {
            val holder = recyclerView.findViewHolderForAdapterPosition(emailPosition)
                    as? FullEmailHolder ?: return
            holder.removeAttachmentAt(attachmentPosition)
        }

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun showMessage(message: UIMessage, showAction: Boolean) {
            if(observer != null){
                SnackBarHelper.show(
                        emailDetailView,
                        context.getLocalizedUIMessage(message), observer as UIObserver,
                        showAction
                )
            }
        }

        override fun showStartGuideEmailIsRead(view: View) {
            observer?.showStartGuideEmailIsRead(view)
        }

        override fun showStartGuideMenu(view: View){
            observer?.showStartGuideMenu(view)
        }
    }

}
