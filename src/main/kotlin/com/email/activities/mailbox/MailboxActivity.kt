package com.email.activities.mailbox

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.DB.AppDatabase
import com.email.DB.seeders.EmailLabelSeeder
import com.email.DB.seeders.EmailSeeder
import com.email.DB.seeders.LabelSeeder
import com.email.R
import com.email.activities.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/23/18.
 */
class MailboxActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerAdapter: EmailAdapter
    lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getAppDatabase(this.applicationContext)!!
        LabelSeeder.seed(db!!.labelDao())
        EmailSeeder.seed(db!!.emailDao())
        EmailLabelSeeder.seed(db!!.emailLabelDao())
        setContentView(R.layout.activity_mailbox)
        recyclerView = findViewById(R.id.mailbox_recycler) as RecyclerView
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerAdapter = EmailAdapter(MailboxDataSource(AppDatabase.getAppDatabase(applicationContext)!!).getEmailThreads()!!)
        recyclerView.adapter = recyclerAdapter
    }
}
