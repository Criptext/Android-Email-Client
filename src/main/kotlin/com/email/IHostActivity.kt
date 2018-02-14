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
    fun showMultiModeBar( selectedThreadsQuantity: Int)
    fun hideMultiModeBar()
    fun updateToolbarTitle()
    fun addToolbar(toolbar: Toolbar)
    fun refreshToolbarItems()

    interface IActivityMenu {
        fun findItemById(id: Int): MenuItem?
    }

    fun setToolbarNumberOfEmails(emailsSize: Int)
}
