package com.email.scenes.emaildetail

import android.content.Context
import com.email.IHostActivity
import com.email.R
import com.email.db.DeliveryTypes
import com.email.db.models.FullEmail
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerTypes
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.labelChooser.LabelDataHandler
import com.email.scenes.labelChooser.SelectedLabels
import com.email.scenes.mailbox.OnMoveThreadsListener
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.data.MailboxRequest
import com.email.scenes.mailbox.data.MailboxResult
import com.email.scenes.params.ComposerParams
import com.email.utils.KeyboardManager
import com.email.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneController(private val scene: EmailDetailScene,
                                 private val model: EmailDetailSceneModel,
                                 private val host: IHostActivity,
                                 private val mailboxDataSource: MailboxDataSource,
                                 private val dataSource: EmailDetailDataSource,
                                 private val keyboard: KeyboardManager?) : SceneController() {

    val lastIndexElement: Int by lazy {
        model.fullEmailList.size - 1
    }

    private val mailboxDataSourceListener = {result: MailboxResult ->
        when (result) {
            is MailboxResult.GetSelectedLabels -> onLabelsLoaded(result)
        }

    }
    private val dataSourceListener = { result: EmailDetailResult ->
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId -> onFullEmailsLoaded(result)
            is EmailDetailResult.UnsendFullEmailFromEmailId -> onUnsendEmail(result)
        }
    }

    private fun onLabelsLoaded(result: MailboxResult.GetSelectedLabels) {
        when (result) {
            is MailboxResult.GetSelectedLabels.Success -> {
                scene.onFetchedLabels(result.selectedLabels,
                        result.allLabels)
            }

            is MailboxResult.GetSelectedLabels.Failure -> {

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
            TODO("MOVE TO SPAM")
        }

        override fun moveToTrash() {
            TODO("MOVE TO TRASH")
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
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDeleteOptionSelected(fullEmail: FullEmail, position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onToggleFullEmailSelection(context: Context, fullEmail: FullEmail, position: Int) {
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
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        mailboxDataSource.listener = mailboxDataSourceListener

        val req = EmailDetailRequest.LoadFullEmailsFromThreadId(
                threadId = model.threadId)

        dataSource.submitRequest(req)
        keyboard?.hideKeyboard()
        return false
    }

    override fun onStop() {
        dataSource.listener = null
        mailboxDataSource.listener = null
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    private fun archiveSelectedThread() {
        val threadId = model.threadId
        TODO("ARCHIVE SELECTED THREAD")
    }
    private fun deleteSelectedThread() {
        val threadId = model.threadId
        TODO("DELETE SELECTED THREAD")
    }
    override fun onOptionsItemSelected(itemId: Int) {
        when(itemId) {
            R.id.mailbox_archive_selected_messages -> archiveSelectedThread()
            R.id.mailbox_delete_selected_messages -> deleteSelectedThread()
            R.id.mailbox_message_toggle_read -> {
                TODO("MESSAGE TOGGLE READ")
            }
            R.id.mailbox_move_to -> {
                scene.showDialogMoveTo(onMoveThreadsListener)
            }
            R.id.mailbox_add_labels -> {
                showLabelsDialog()
            }
        }
    }

    private fun showLabelsDialog() {
        val req = MailboxRequest.GetSelectedLabels(
                threadIds = listOf(model.threadId)
        )

        mailboxDataSource.submitRequest(req)
        scene.showDialogLabelsChooser(LabelDataHandler(this))
    }
    fun createRelationSelectedEmailLabels(selectedLabels: SelectedLabels) {
        TODO("""START WORKER, SHOW GENERIC DIALOG,
            ON FINISH WORKER, HIDE GENERIC DIALOG. """)

    }
    override val menuResourceId: Int?
        get() = R.menu.mailbox_menu_multi_mode_read
}
