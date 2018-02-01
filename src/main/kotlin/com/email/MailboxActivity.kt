package com.email

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.email.DB.MailboxLocalDB
import com.email.androidui.SceneFactory
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : AppCompatActivity(), IHostActivity {

    private lateinit var sceneFactory : SceneFactory

    private lateinit var mailboxSceneController: MailboxSceneController
    private var mailboxSceneModel : MailboxSceneModel = MailboxSceneModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)

        sceneFactory = SceneFactory.SceneInflater(this, mailboxSceneModel.getThreadFromIndex, mailboxSceneModel.getEmailThreadsCount)

        initController()
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
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.mailbox_search -> {
                TODO("HANDLE SEARCH CLICK...")
                return true
            }

            R.id.mailbox_bell_container -> {
                TODO("HANDLE BELL CLICK...")
                return true
            }
            R.id.mailbox_archive_selected_messages -> {
                mailboxSceneController.archiveSelectedEmailThreads()
                return true
            }
            R.id.mailbox_delete_selected_messages -> {
                TODO("HANDLE DELETE")
                mailboxSceneController.deleteSelectedEmailThreads()
                return true
            }

            R.id.mailbox_toggle_read_selected_messages -> {
                TODO("HANDLE TOGGLE READ SELECTED MESSAGES")
                mailboxSceneController.toggleReadSelectedEmailThreads()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(!mailboxSceneModel.isInMultiSelect) {
            menu?.clear()
            menuInflater.inflate(R.menu.mailbox_menu_normal_mode, menu) // rendering normal mode items...
        } else {
            menu?.clear()
           menuInflater.inflate(R.menu.mailbox_menu_multi_mode, menu) // rendering multi mode items...
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        mailboxSceneController.onBackPressed(this)
    }
}