package com.email

import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.SelectedThreads

/**
 * Created by sebas on 1/29/18.
 */
interface IHostActivity {
    fun initController()
    fun getMailboxSceneController() : MailboxSceneController
    fun getSelectedThreads() : SelectedThreads
    fun showMultiModeBar( selectedThreadsQuantity: Int)
    fun hideMultiModeBar()
    fun updateToolbarTitle(title: String)
    fun addToolbar(toolbar: Toolbar)
    fun refreshToolbarItems()
    fun showDialogLabelChooser()

    interface IActivityMenu {
        fun findItemById(id: Int): MenuItem?
    }
}
