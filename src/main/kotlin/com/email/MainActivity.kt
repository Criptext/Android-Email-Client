package com.email

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.email.DB.MailboxLocalDB
import com.email.androidui.SceneFactory

/**
 * Created by sebas on 1/30/18.
 */

class MainActivity: AppCompatActivity(), IHostActivity {
    override val activity: Activity by lazy {
        this
    }

    override val database: MailboxLocalDB by lazy {
        MailboxLocalDB.Default(applicationContext)
    }

    private lateinit var manager: SceneManager

    // creating default mailbox scene...
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)
        SceneFactory.SceneInflater(this).createMailboxScene()
        manager = SceneManager(this)
    }

    override fun onStart() {
        super.onStart()
        manager.onStart()
    }
}