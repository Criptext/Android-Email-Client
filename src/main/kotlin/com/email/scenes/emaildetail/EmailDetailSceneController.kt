package com.email.scenes.emaildetail

import com.email.IHostActivity
import com.email.R
import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.models.FullEmail
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerTypes
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.label_chooser.LabelDataHandler
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.mailbox.OnMoveThreadsListener
import com.email.scenes.params.ComposerParams
import com.email.utils.KeyboardManager
import com.email.utils.virtuallist.VirtualList
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneController(private val scene: EmailDetailScene,
                                 private val model: EmailDetailSceneModel,
                                 private val host: IHostActivity,
                                 private val dataSource: EmailDetailDataSource,
                                 private val keyboard: KeyboardManager?) : SceneController() {

    val lastIndexElement: Int by lazy {
        model.fullEmailList.size - 1
    }

    private val dataSourceListener = { result: EmailDetailResult ->
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId -> onFullEmailsLoaded(result)
            is EmailDetailResult.UnsendFullEmailFromEmailId -> onUnsendEmail(result)
            is EmailDetailResult.GetSelectedLabels -> onSelectedLabelsLoaded(result)
            is EmailDetailResult.UpdateEmailThreadsLabelsRelations -> onUpdatedLabels(result)
            is EmailDetailResult.UpdateUnreadStatus -> onUpdateUnreadStatus(result)
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
                host.finishScene()
            } else -> {
                scene.showError(UIMessage(R.string.error_updating_labels))
            }
        }
    }

    private fun onUpdateUnreadStatus(result: EmailDetailResult.UpdateUnreadStatus){
        when(result) {
            is EmailDetailResult.UpdateUnreadStatus.Success ->  {
                host.finishScene()
            } else -> {
                scene.showError(UIMessage(R.string.error_updating_status))
            }
        }
    }

    private fun onUnsendEmail(result: EmailDetailResult.UnsendFullEmailFromEmailId) {
        when (result) {
            is EmailDetailResult.UnsendFullEmailFromEmailId.Success -> {
                model.fullEmailList[result.position].email.delivered = DeliveryTypes.UNSENT
                scene.notifyFullEmailChanged(result.position)
            }

            is EmailDetailResult.UnsendFullEmailFromEmailId.Failure -> {
            }
        }
    }

    private val onMoveThreadsListener = object : OnMoveThreadsListener {
        override fun moveToSpam() {
            createRelationAllEmailLabels(null, MailFolders.SPAM)
        }

        override fun moveToTrash() {
            createRelationAllEmailLabels(null, MailFolders.TRASH)
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
                    fullEmail = model.fullEmailList[lastIndexElement],
                    composerType = ComposerTypes.FORWARD), true)
        }

        override fun onReplyBtnClicked() {
            host.goToScene(ComposerParams(
                    fullEmail = model.fullEmailList[lastIndexElement],
                    composerType = ComposerTypes.REPLY), true)
        }

        override fun onReplyAllBtnClicked() {
            host.goToScene(ComposerParams(
                    fullEmail = model.fullEmailList[lastIndexElement],
                    composerType = ComposerTypes.REPLY_ALL), true)
        }

        override fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean) {
            fullEmail.viewOpen = viewOpen
            if(viewOpen) {
            }

            scene.notifyFullEmailChanged(position)
        }

        override fun onReplyOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            host.goToScene(ComposerParams(fullEmail = fullEmail, composerType = ComposerTypes.REPLY), true)
        }

        override fun onReplyAllOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            host.goToScene(ComposerParams(fullEmail = fullEmail, composerType = ComposerTypes.REPLY_ALL), true)
        }

        override fun onForwardOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            host.goToScene(ComposerParams(fullEmail = fullEmail, composerType = ComposerTypes.FORWARD), true)
        }

        override fun onToggleReadOption(fullEmail: FullEmail, position: Int, markAsRead: Boolean) {
            dataSource.submitRequest(EmailDetailRequest.UpdateUnreadStatus(
                    threadId = model.threadId,
                    updateUnreadStatus = true))
        }

        override fun onDeleteOptionSelected(fullEmail: FullEmail, position: Int) {
            createRelationEmailLabel(fullEmail, MailFolders.TRASH)
        }

        override fun onSpamOptionSelected(fullEmail: FullEmail, position: Int) {
            createRelationEmailLabel(fullEmail, MailFolders.SPAM)
        }

        override fun onContinueDraftOptionSelected(fullEmail: FullEmail) {
            host.goToScene(ComposerParams(fullEmail = fullEmail, composerType = ComposerTypes.CONTINUE_DRAFT), true)
        }
    }

    private fun onFullEmailsLoaded(result: EmailDetailResult.LoadFullEmailsFromThreadId){
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId.Success -> {

                val fullEmailsList = VirtualList.Map(result.fullEmailList, { t -> t })
                model.fullEmailList = fullEmailsList

                scene.attachView(
                        fullEmailList = fullEmailsList,
                        fullEmailEventListener = emailHolderEventListener)
            }

            is EmailDetailResult.LoadFullEmailsFromThreadId.Failure -> {
                scene.showError(UIMessage(R.string.error_getting_email))
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener

        val req = EmailDetailRequest.LoadFullEmailsFromThreadId(
                threadId = model.threadId)

        dataSource.submitRequest(req)
        keyboard?.hideKeyboard()
        return false
    }

    override fun onStop() {
        dataSource.listener = null
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    private fun archiveThread() {
        createRelationAllEmailLabels(null, null)
    }

    private fun deleteThread() {
        createRelationAllEmailLabels(null, MailFolders.TRASH)
    }

    private fun updateUnreadStatusThread(){
        dataSource.submitRequest(EmailDetailRequest.UpdateUnreadStatus(
                threadId = model.threadId,
                updateUnreadStatus = true))
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when(itemId) {
            R.id.mailbox_archive_selected_messages -> archiveThread()
            R.id.mailbox_delete_selected_messages -> deleteThread()
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

    fun createRelationEmailLabel(fullEmail: FullEmail, chosenLabel: MailFolders){

        val req = EmailDetailRequest.UpdateEmailThreadsLabelsRelations(
                threadId = model.threadId,
                selectedLabels = null,
                chosenLabel = chosenLabel)

        dataSource.submitRequest(req)
    }

    fun createRelationAllEmailLabels(selectedLabels: SelectedLabels?, chosenLabel: MailFolders?) {

        val req = EmailDetailRequest.UpdateEmailThreadsLabelsRelations(
                threadId = model.threadId,
                selectedLabels = selectedLabels,
                chosenLabel = chosenLabel)

        dataSource.submitRequest(req)

    }

    override val menuResourceId: Int?
        get() = R.menu.mailbox_menu_multi_mode_read
}
