package com.email

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.DB.MailboxLocalDB
import com.email.androidui.SceneFactory
import com.email.scenes.LabelChooser.DialogLabelsChooser
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.mailbox.SelectedThreads
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : AppCompatActivity(), IHostActivity {

    private lateinit var sceneFactory : SceneFactory
    private lateinit var dialogLabelsChooser : DialogLabelsChooser
    private lateinit var mailboxSceneController: MailboxSceneController
    private var mailboxSceneModel : MailboxSceneModel = MailboxSceneModel()
    private val threadListHandler : ThreadListHandler = ThreadListHandler(mailboxSceneModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)
        sceneFactory = SceneFactory.SceneInflater(this,
                threadListHandler)
        dialogLabelsChooser = DialogLabelsChooser.Builder().build(sceneFactory)
        initController()
    }

    override fun initController() {
        val DB : MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        mailboxSceneController = MailboxSceneController(
                scene = sceneFactory.createMailboxScene(),
                model = mailboxSceneModel,
                dataSource = MailboxDataSource(DB))
    }

/*    fun startLabelChooserDialog() {
        val DB : MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        labelChooserSceneController = LabelChooserSceneController(
                scene = sceneFactory.createChooserDialogScene(),
                model = labelChooserSceneModel,
                dataSource = LabelChooserDataSource(DB))

        labelChooserSceneController.onStart()
    }*/

    override fun onStart() {
        super.onStart()
        mailboxSceneController.onStart()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return mailboxSceneController.onOptionSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu == null) return false
        if(!mailboxSceneModel.isInMultiSelect) {
            menu.clear()
            menuInflater.inflate(R.menu.mailbox_menu_normal_mode, menu) // rendering normal mode items...
        } else {
            menu.clear()
            menuInflater.inflate(R.menu.mailbox_menu_multi_mode, menu) // rendering multi mode items...
            mailboxSceneController.addTintInMultiMode(this, menu.findItem(R.id.mailbox_delete_selected_messages))
            mailboxSceneController.addTintInMultiMode(this, menu.findItem(R.id.mailbox_archive_selected_messages))
            mailboxSceneController.addTintInMultiMode(this, menu.findItem(R.id.mailbox_toggle_read_selected_messages))
        }
        mailboxSceneController.toggleMultiModeBar()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        mailboxSceneController.onBackPressed(this)
    }


    override fun showMultiModeBar(selectedThreadsQuantity: Int) {
        this.findViewById<ImageView>(R.id.mailbox_nav_button).visibility = View.GONE
        this.
                findViewById<TextView>(R.id.mailbox_number_emails)
                .visibility = View.GONE
        this.
                findViewById<TextView>(R.id.mailbox_toolbar_title).
                text = selectedThreadsQuantity.toString()
    }

    override fun hideMultiModeBar() {
        this.findViewById<ImageView>(R.id.mailbox_nav_button)
                .visibility = View.VISIBLE
        this.findViewById<TextView>(R.id.mailbox_number_emails)
                .visibility = View.VISIBLE
        this.findViewById<TextView>(R.id.mailbox_toolbar_title).text = "INBOX"
    }

    override fun updateToolbarTitle(title: String) {
        this.findViewById<TextView>(R.id.mailbox_toolbar_title).text = title
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