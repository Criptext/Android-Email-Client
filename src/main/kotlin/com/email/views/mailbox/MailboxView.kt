package com.email.views.mailbox

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.email.DB.MailboxLocalDB
import com.email.R
import com.email.views.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/23/18.
 */
class MailboxView : AppCompatActivity() {

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
}
