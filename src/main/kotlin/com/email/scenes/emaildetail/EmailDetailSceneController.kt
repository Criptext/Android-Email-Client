package com.email.scenes.emaildetail

import com.email.IHostActivity
import com.email.R
import com.email.bgworker.BackgroundWorkManager
import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.models.ActiveAccount
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerTypes
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.label_chooser.LabelDataHandler
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.mailbox.OnDeleteThreadListener
import com.email.scenes.mailbox.OnMoveThreadsListener
import com.email.scenes.params.ComposerParams
import com.email.scenes.params.MailboxParams
import com.email.utils.KeyboardManager
import com.email.utils.virtuallist.VirtualList
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneController(private val scene: EmailDetailScene,
                                 private val model: EmailDetailSceneModel,
                                 private val host: IHostActivity,
                                 activeAccount: ActiveAccount,
                                 private val dataSource: BackgroundWorkManager<EmailDetailRequest, EmailDetailResult>,
                                 private val keyboard: KeyboardManager) : SceneController() {

    private val dataSourceListener = { result: EmailDetailResult ->
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId -> onFullEmailsLoaded(result)
            is EmailDetailResult.UnsendFullEmailFromEmailId -> onUnsendEmail(result)
            is EmailDetailResult.GetSelectedLabels -> onSelectedLabelsLoaded(result)
            is EmailDetailResult.UpdateEmailThreadsLabelsRelations -> onUpdatedLabels(result)
            is EmailDetailResult.UpdateUnreadStatus -> onUpdateUnreadStatus(result)
            is EmailDetailResult.MoveEmailThread -> onMoveEmailThread(result)
        }
    }

    private fun onSelectedLabelsLoaded(result: EmailDetailResult.GetSelectedLabels) {
        when (result) {
            is EmailDetailResult.GetSelectedLabels.Success -> {
                scene.onFetchedSelectedLabels(result.selectedLabels,
                        result.allLabels)
            }

            is EmailDetailResult.GetSelectedLabels.Failure -> {
                scene.showError(UIMessage(R.string.error_getting_labels))
            }
        }
    }

    private fun onUpdatedLabels(result: EmailDetailResult.UpdateEmailThreadsLabelsRelations) {

        when(result) {
            is EmailDetailResult.UpdateEmailThreadsLabelsRelations.Success ->  {
                val message = ActivityMessage.UpdateLabelsThread(
                        threadId = result.threadId,
                        selectedLabelIds = result.selectedLabelIds
                )
                host.exitToScene(MailboxParams(), message)
            } else -> {
                scene.showError(UIMessage(R.string.error_updating_labels))
            }
        }
    }

    private fun onUpdateUnreadStatus(result: EmailDetailResult.UpdateUnreadStatus){
        when(result) {
            is EmailDetailResult.UpdateUnreadStatus.Success ->  {
                val message = ActivityMessage.UpdateUnreadStatusThread(result.threadId, result.unread)
                host.exitToScene(MailboxParams(), message)
            } else -> {
                scene.showError(UIMessage(R.string.error_updating_status))
            }
        }
    }

    private fun onMoveEmailThread(result: EmailDetailResult.MoveEmailThread){
        when(result) {
            is EmailDetailResult.MoveEmailThread.Success ->  {
                val message = ActivityMessage.MoveThread(result.threadId)
                host.exitToScene(MailboxParams(), message)
            } else -> {
                scene.showError(UIMessage(R.string.error_moving_emails))
            }
        }
    }

    private fun onUnsendEmail(result: EmailDetailResult.UnsendFullEmailFromEmailId) {
        when (result) {
            is EmailDetailResult.UnsendFullEmailFromEmailId.Success -> {
                model.emails[result.position].email.delivered = DeliveryTypes.UNSENT
                scene.notifyFullEmailChanged(result.position)
            }

            is EmailDetailResult.UnsendFullEmailFromEmailId.Failure -> {
            }
        }
    }

    private val onMoveThreadsListener = object : OnMoveThreadsListener {

        override fun onMoveToInboxClicked() {
            moveEmailThread(MailFolders.INBOX)
        }

        override fun onMoveToSpamClicked() {
            moveEmailThread(MailFolders.SPAM)
        }

        override fun onMoveToTrashClicked() {
            moveEmailThread(MailFolders.TRASH)
        }
    }

    private val onDeleteThreadListener = object : OnDeleteThreadListener {
        override fun onDeleteConfirmed() {
            moveEmailThread(chosenLabel = null)
        }
    }

    private val emailHolderEventListener = object : FullEmailListAdapter.OnFullEmailEventListener{

        override fun onUnsendEmail(fullEmail: FullEmail, position: Int) {
            val req = EmailDetailRequest.UnsendFullEmailFromEmailId(
                    position = position,
                    emailId = fullEmail.email.id)

            dataSource.submitRequest(req)
        }
        override fun onForwardBtnClicked() {
            host.goToScene(ComposerParams(
                    fullEmail = model.emails.last(),
                    composerType = ComposerTypes.FORWARD,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }

        override fun onReplyBtnClicked() {
            host.goToScene(ComposerParams(
                    fullEmail = model.emails.last(),
                    composerType = ComposerTypes.REPLY,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }

        override fun onReplyAllBtnClicked() {
            host.goToScene(ComposerParams(
                    fullEmail = model.emails.last(),
                    composerType = ComposerTypes.REPLY_ALL,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }

        override fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean) {
            fullEmail.viewOpen = viewOpen
            if(viewOpen) {
            }

            scene.notifyFullEmailChanged(position)
        }

        override fun onReplyOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            host.goToScene(ComposerParams(
                    fullEmail = fullEmail,
                    composerType = ComposerTypes.REPLY,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }

        override fun onReplyAllOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            host.goToScene(ComposerParams(
                    fullEmail = fullEmail,
                    composerType = ComposerTypes.REPLY_ALL,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }

        override fun onForwardOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            host.goToScene(ComposerParams(
                    fullEmail = fullEmail,
                    composerType = ComposerTypes.FORWARD,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }

        override fun onToggleReadOption(fullEmail: FullEmail, position: Int, markAsRead: Boolean) {
            dataSource.submitRequest(EmailDetailRequest.UpdateUnreadStatus(
                    threadId = model.threadId,
                    updateUnreadStatus = true,
                    currentLabel = model.currentLabel))
        }

        override fun onDeleteOptionSelected(fullEmail: FullEmail, position: Int) {
            moveEmail(fullEmail, MailFolders.TRASH)
        }

        override fun onSpamOptionSelected(fullEmail: FullEmail, position: Int) {
            moveEmail(fullEmail, MailFolders.SPAM)
        }

        override fun onContinueDraftOptionSelected(fullEmail: FullEmail) {
            host.goToScene(ComposerParams(
                    fullEmail = fullEmail,
                    composerType = ComposerTypes.CONTINUE_DRAFT,
                    userEmail = activeAccount.userEmail,
                    emailDetailActivity = host), true)
        }
    }

    private fun readEmails(emails: List<FullEmail>) {
        val emailIds = emails.map { it.email.id }
        val metadataKeys = emails.map { it.email.id } //TODO use the REAL metadata key

        dataSource.submitRequest(EmailDetailRequest.ReadEmails(
                emailIds = emailIds,
                metadataKeys = metadataKeys
        ))
    }

    private fun onFullEmailsLoaded(result: EmailDetailResult.LoadFullEmailsFromThreadId){
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId.Success -> {

                model.emails.addAll(result.fullEmailList)
                val fullEmailsList = VirtualList.Map(result.fullEmailList, { t -> t })

                scene.attachView(
                        fullEmailList = fullEmailsList,
                        fullEmailEventListener = emailHolderEventListener)

                if (result.fullEmailList.isNotEmpty())
                    readEmails(result.fullEmailList)
            }

            is EmailDetailResult.LoadFullEmailsFromThreadId.Failure -> {
                scene.showError(UIMessage(R.string.error_getting_email))
            }
        }
    }

    private fun loadEmails() {
        val req = EmailDetailRequest.LoadFullEmailsFromThreadId(
                threadId = model.threadId, currentLabel = model.currentLabel)

        dataSource.submitRequest(req)
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener

        if (model.emails.isEmpty())
            loadEmails()

        keyboard.hideKeyboard()
        return false
    }

    override fun onStop() {
        dataSource.listener = null
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    private fun removeCurrentLabelThread() {
        val req = EmailDetailRequest.UpdateEmailThreadsLabelsRelations(
                threadId = model.threadId,
                selectedLabels = SelectedLabels(),
                currentLabel = model.currentLabel,
                removeCurrentLabel = true)

        dataSource.submitRequest(req)
    }

    private fun deleteThread() {
        moveEmailThread(MailFolders.TRASH)
    }

    private fun updateUnreadStatusThread(){
        dataSource.submitRequest(EmailDetailRequest.UpdateUnreadStatus(
                threadId = model.threadId,
                updateUnreadStatus = true,
                currentLabel = model.currentLabel))
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when(itemId) {
            R.id.mailbox_archive_selected_messages -> removeCurrentLabelThread()
            R.id.mailbox_delete_selected_messages -> deleteThread()
            R.id.mailbox_delete_selected_messages_4ever -> deleteSelectedEmailThreads4Ever()
            R.id.mailbox_not_spam -> removeCurrentLabelThread()
            R.id.mailbox_not_trash -> removeCurrentLabelThread()
            R.id.mailbox_spam -> moveEmailThread(MailFolders.SPAM)
            R.id.mailbox_message_toggle_read -> updateUnreadStatusThread()
            R.id.mailbox_move_to -> {
                scene.showDialogMoveTo(onMoveThreadsListener)
            }
            R.id.mailbox_add_labels -> {
                showLabelsDialog()
            }
        }
    }

    private fun showLabelsDialog() {
        val req = EmailDetailRequest.GetSelectedLabels(model.threadId)
        dataSource.submitRequest(req)
        scene.showDialogLabelsChooser(LabelDataHandler(this))
    }

    fun moveEmail(fullEmail: FullEmail, chosenLabel: MailFolders){

        val req = EmailDetailRequest.MoveEmail(
                emailId = fullEmail.email.id,
                chosenLabel = chosenLabel,
                currentLabel = model.currentLabel)

        dataSource.submitRequest(req)
    }

    fun updateThreadLabelsRelation(selectedLabels: SelectedLabels) {

        val req = EmailDetailRequest.UpdateEmailThreadsLabelsRelations(
                threadId = model.threadId,
                selectedLabels = selectedLabels,
                currentLabel = model.currentLabel,
                removeCurrentLabel = false)

        dataSource.submitRequest(req)

    }

    private fun moveEmailThread(chosenLabel: MailFolders?) {
        val req = EmailDetailRequest.MoveEmailThread(
                threadId = model.threadId,
                chosenLabel = chosenLabel,
                currentLabel = model.currentLabel)

        dataSource.submitRequest(req)
    }

    private fun deleteSelectedEmailThreads4Ever() {
        scene.showDialogDeleteThread(onDeleteThreadListener)
    }

    override val menuResourceId: Int?
        get() = when {
            model.currentLabel == Label.defaultItems.draft -> R.menu.mailbox_menu_multi_mode_read_draft
            model.currentLabel == Label.defaultItems.spam -> R.menu.mailbox_menu_multi_mode_read_spam
            model.currentLabel == Label.defaultItems.trash -> R.menu.mailbox_menu_multi_mode_read_trash
            model.currentLabel.id < 0 -> R.menu.mailbox_menu_multi_mode_read_allmail
            else -> R.menu.mailbox_menu_multi_mode_read
        }
}
