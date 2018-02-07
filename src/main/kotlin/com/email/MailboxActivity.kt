package com.email

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.email.DB.MailboxLocalDB
import com.email.androidui.SceneFactory
import com.email.scenes.LabelChooser.DialogLabelsChooser
import com.email.scenes.LabelChooser.SelectedLabels
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.mailbox.SelectedThreads
import com.email.scenes.mailbox.ToolbarController
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.holders.ToolbarHolder

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : AppCompatActivity(), IHostActivity {

    private lateinit var sceneFactory : SceneFactory
    private lateinit var dialogLabelsChooser : DialogLabelsChooser
    private lateinit var mailboxSceneController: MailboxSceneController
    private lateinit var toolbarController: ToolbarController
    private lateinit var toolbarHolder: ToolbarHolder
    private var mailboxSceneModel : MailboxSceneModel = MailboxSceneModel()
    private val threadListHandler : ThreadListHandler = ThreadListHandler(mailboxSceneModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)
        sceneFactory = SceneFactory.SceneInflater(this,
                threadListHandler)

        initController()
        val labelDataSourceHandler: LabelDataSourceHandler =
                LabelDataSourceHandler(mailboxSceneController)
        dialogLabelsChooser = DialogLabelsChooser
                .Builder()
                .build(sceneFactory, labelDataSourceHandler)
    }
    override fun initController() {
        val DB : MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        mailboxSceneController = MailboxSceneController(
                scene = sceneFactory.createMailboxScene(),
                model = mailboxSceneModel,
                dataSource = MailboxDataSource(DB))
    }

    override fun onStart() {
        super.onStart()
        mailboxSceneController.onStart()
        toolbarHolder = ToolbarHolder(mailboxSceneController.getToolbar())
        toolbarController = ToolbarController(toolbarHolder)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return mailboxSceneController.onOptionSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu == null) return false
        return try {
            val activeSceneMenu = mailboxSceneController.menuResourceId
            if (activeSceneMenu != null) {
                menuInflater.inflate(activeSceneMenu, menu)
                mailboxSceneController.postMenuDisplay(menu)
                mailboxSceneController.toggleMultiModeBar()
                true
            } else
                super.onCreateOptionsMenu(menu)
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

    override fun updateToolbarTitle(title: String) {
        toolbarController.updateToolbarTitle(title)
    }

    override fun addToolbar(toolbar: Toolbar) {
        this.setSupportActionBar(toolbar)
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

    inner class ThreadListHandler(val model: MailboxSceneModel) {
        val getThreadFromIndex = {
            i: Int ->
            model.threads[i]
        }
        val getEmailThreadsCount = {
            model.threads.size
        }
    }
    inner class LabelDataSourceHandler(mailboxSceneController: MailboxSceneController) {
        val createRelationEmailLabels = {
            selectedLabels: SelectedLabels ->
            mailboxSceneController.createRelationSelectedEmailLabels(selectedLabels)
        }

        val createLabelEmailRelation = {
            labelId: Int, emailThreadId: Int ->
            mailboxSceneController.assignLabelToEmailThread(labelId,
                    emailThreadId)
        }

        val getAllLabels = {
            mailboxSceneController.getAllLabels()
        }
    }

    override fun showDialogLabelChooser() {
        dialogLabelsChooser.show(supportFragmentManager, "")
    }

    override fun getMailboxSceneController() : MailboxSceneController{
        return this.mailboxSceneController
    }

    override fun getSelectedThreads() : SelectedThreads{
        return this.mailboxSceneModel.selectedThreads
    }
}