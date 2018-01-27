package com.email.views.mailbox

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
    private val mailboxLocalDB : MailboxLocalDB = MailboxLocalDB.Default(applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)
        mailboxLocalDB.seed()
        recyclerView = findViewById(R.id.mailbox_recycler) as RecyclerView
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerAdapter = EmailThreadAdapter(MailboxDataSource(mailboxLocalDB).getEmailThreads())
        recyclerView.adapter = recyclerAdapter
    }
}
