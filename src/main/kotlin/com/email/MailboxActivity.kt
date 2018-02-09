package com.email

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.email.DB.MailboxLocalDB
import com.email.scenes.LabelChooser.LabelDataSourceHandler
import com.email.scenes.mailbox.*
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.holders.ToolbarHolder
import com.email.utils.VirtualList

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : AppCompatActivity(), IHostActivity {

    private lateinit var mailboxSceneController: MailboxSceneController
    private lateinit var toolbarController: ToolbarController
    private lateinit var toolbarHolder: ToolbarHolder

    lateinit var labelDataSourceHandler: LabelDataSourceHandler
    lateinit var onMoveThreadsListener: OnMoveThreadsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)
        initController()
        labelDataSourceHandler = LabelDataSourceHandler(mailboxSceneController)
        onMoveThreadsListener = OnMoveThreadsListener(mailboxSceneController)
    }
    override fun initController() {
        val DB : MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        val model = MailboxSceneModel()
        val rootView = findViewById<ViewGroup>(R.id.scene_container)
        val scene = MailboxScene.MailboxSceneView(rootView, this,
                VirtualEmailThreadList(model.threads))
        mailboxSceneController = MailboxSceneController(
                scene = scene,
                model = model,
                dataSource = MailboxDataSource(DB))
    }

    override fun onStart() {
        super.onStart()
        mailboxSceneController.onStart()
        toolbarHolder = ToolbarHolder(mailboxSceneController.getToolbar())
        toolbarController = ToolbarController(toolbarHolder)
        toolbarController.onStart(mailboxSceneController.emailThreadSize)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return mailboxSceneController.onOptionSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu == null) return false
        return try {
            val activeSceneMenu = mailboxSceneController.menuResourceId
            menuInflater.inflate(activeSceneMenu, menu)
            mailboxSceneController.postMenuDisplay(menu)
            true
        }
        catch (e : UninitializedPropertyAccessException) {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun onBackPressed() {
        mailboxSceneController.onBackPressed(this)
    }


    override fun showMultiModeBar(selectedThreadsQuantity: Int) {
        toolbarController.showMultiModeBar(selectedThreadsQuantity)
    }

    override fun hideMultiModeBar() {
        toolbarController.hideMultiModeBar()
    }

    override fun updateToolbarTitle() {
        toolbarController.updateToolbarTitle(mailboxSceneController.toolbarTitle)
    }

    override fun setToolbarNumberOfEmails(emailsSize: Int) {
        toolbarController.updateNumerOfMails(emailsSize)
    }

    override fun addToolbar(toolbar: Toolbar) {
        this.setSupportActionBar(toolbar)
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

    override fun getMailboxSceneController() : MailboxSceneController{
        return this.mailboxSceneController
    }

    private class VirtualEmailThreadList(val threads: ArrayList<EmailThread>)
        : VirtualList<EmailThread>  {
        override fun get(i: Int) = threads[i]

        override val size: Int
            get() = threads.size
    }
}