package com.email.scenes.emaildetail

import android.content.Context
import com.email.IHostActivity
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.SceneController
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.labelChooser.LabelDataSourceHandler
import com.email.scenes.labelChooser.SelectedLabels
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneController(private val scene: EmailDetailScene,
                                 private val model: EmailDetailSceneModel,
                                 private val host: IHostActivity,
                                 private val dataSource: EmailDetailDataSource) : SceneController() {

    private val dataSourceListener = { result: EmailDetailResult ->
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId -> onFullEmailsLoaded(result)
        }
    }

    private val emailHolderEventListener = object : FullEmailListAdapter.OnFullEmailEventListener{
        override fun onForwardBtnClicked() {
            TODO("on forward btn clicked") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onReplyBtnClicked() {
            TODO("on reply btn clicked") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onReplyAllBtnClicked() {
            TODO("on replyAll btn clicked") //To change body of created functions use File | Settings | File Templates.
        }

        override fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean) {
                fullEmail.viewOpen = viewOpen

                scene.notifyFullEmailChanged(position)
                scene.notifyFullEmailListChanged()
        }

        override fun onReplyOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    override fun onStart() {
        dataSource.listener = dataSourceListener

        val req = EmailDetailRequest.LoadFullEmailsFromThreadId(
                threadId = model.threadId)

        dataSource.submitRequest(req)
    }

    override fun onStop() {
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
                TODO("mailbox_move to")
            }
            R.id.mailbox_add_labels -> {
                scene.showDialogLabelsChooser(LabelDataSourceHandler(this))
            }
        }
    }

    fun createRelationSelectedEmailLabels(selectedLabels: SelectedLabels) {
        TODO("""START WORKER, SHOW GENERIC DIALOG,
            ON FINISH WORKER, HIDE GENERIC DIALOG. """)

    }
    override val menuResourceId: Int?
        get() = R.menu.mailbox_menu_multi_mode_read
}
