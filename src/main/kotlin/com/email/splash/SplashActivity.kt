package com.email.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.email.*
import com.email.db.KeyValueStorage
import java.lang.ref.WeakReference

/**
 * Created by gesuwall on 3/27/17.
 */

class SplashActivity: AppCompatActivity(), WelcomeTimeout.Listener {

    private var welcomeTimeout: WelcomeTimeout? = null
    private val storage: KeyValueStorage by lazy {
        KeyValueStorage.SharedPrefs(this.applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        welcomeTimeout = WelcomeTimeout(2000L, this)
        welcomeTimeout!!.start()
    }

    private fun hasActiveAccount(): Boolean =
        storage.getString(KeyValueStorage.StringKey.ActiveAccount, "") != ""

    override fun onTimeout() {
        finish()
        if(hasActiveAccount()){
            startActivity(Intent(this, MailboxActivity::class.java))
            overridePendingTransition(0, 0)
        }
        else{
            startActivity(Intent(this, SignInActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
    }
}

private class WelcomeTimeout(val timeToWait: Long, listener: Listener) {

    val handler = Handler()
    val listenerRef: WeakReference<Listener> = WeakReference(listener)

    fun start() {
        handler.postDelayed({
            listenerRef.get()?.onTimeout()
        }, timeToWait)
    }

    fun cancel() {
        listenerRef.clear()
    }

    interface Listener {
        fun onTimeout()
    }
}