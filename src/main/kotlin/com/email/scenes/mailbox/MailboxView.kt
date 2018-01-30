package com.email.scenes.mailbox

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.email.DB.MailboxLocalDB
import com.email.IHostActivity
import com.email.R
import com.email.androidui.mailthread.ThreadListView
import com.email.androidui.mailthread.ThreadRecyclerView
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/23/18.
 */

interface MailboxScene : ThreadListView{

    fun setEmailList(threads: ArrayList<EmailThread>)
    fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener)

    class MailboxSceneView(private val sceneContainer: ViewGroup, private val mailboxView: View,
                           val hostActivity: IHostActivity) : MailboxScene {

        private val ctx = mailboxView.context

        private val recyclerView: RecyclerView by lazy {
            mailboxView.findViewById(R.id.mailbox_recycler) as RecyclerView
        }
        private val toolbar: Toolbar by lazy {
            mailboxView.findViewById<Toolbar>(R.id.mailbox_toolbar)
        }

        private lateinit var threadRecyclerView: ThreadRecyclerView

        var threadListener: EmailThreadAdapter.OnThreadEventListener? = null
            set(value) {
                threadRecyclerView.setThreadListener(value)
                field = value
            }
        override fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener) {
            threadRecyclerView = ThreadRecyclerView(recyclerView, threadEventListener)
            this.threadListener = threadEventListener

            sceneContainer.removeAllViews()
            sceneContainer.addView(mailboxView)
        }


        override fun setEmailList(threads: ArrayList<EmailThread>) {
            threadRecyclerView.setThreadList(threads)
        }

        override fun notifyThreadSetChanged() {
            threadRecyclerView.notifyThreadSetChanged()
        }

        override fun notifyThreadRemoved(position: Int) {
            threadRecyclerView.notifyThreadRemoved(position)
        }

        override fun notifyThreadChanged(position: Int) {
            threadRecyclerView.notifyThreadChanged(position)
        }

        override fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int) {
            threadRecyclerView.notifyThreadRangeInserted(positionStart, itemCount)
        }
    }
}

/*class MailboxActivity : AppCompatActivity(), IHostActivity {
    override val activity: Activity
        get() = this
    override val database: MailboxLocalDB
        get() = MailboxLocalDB.Default(applicationContext)

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerAdapter: EmailThreadAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var mailboxLocalDB : MailboxLocalDB

    override fun onCreate(savedInstanceState: Bundle?) {
        mailboxLocalDB = MailboxLocalDB.Default(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)
        mailboxLocalDB.seed()
        recyclerView = findViewById(R.id.mailbox_recycler) as RecyclerView
        toolbar = findViewById<Toolbar>(R.id.mailbox_toolbar)

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerAdapter = EmailThreadAdapter(MailboxDataSource(mailboxLocalDB).getEmailThreads())
        recyclerView.adapter = recyclerAdapter

        // Adding simple toolbar to mailbox view.
        addToolbar()

        toolbar.showOverflowMenu()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mailbox_menu, menu)
        Log.d("ACTION BAR", "adding action bar...")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d("CLICK", "Item")
        return true
    }
    fun addToolbar() {
        setSupportActionBar(toolbar)
    }
}*/
