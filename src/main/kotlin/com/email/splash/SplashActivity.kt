package com.email.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.email.*
import java.lang.ref.WeakReference

/**
 * Created by gesuwall on 3/27/17.
 */

class SplashActivity: AppCompatActivity(), WelcomeTimeout.Listener {

    private var welcomeTimeout: WelcomeTimeout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        welcomeTimeout = WelcomeTimeout(2000L, this)
        welcomeTimeout!!.start()
    }

    override fun onTimeout() {

        finish()
        if(hasActiveAccount(this)){
            startActivity(Intent(this, MailboxActivity::class.java))
            overridePendingTransition(0, 0)
        }
        else{
            startActivity(Intent(this, MailboxActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
    }

    companion object {
        fun hasActiveAccount(ctx: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)
            return prefs.getString(SecureEmail.ACTIVE_ACCOUNT, "") != ""
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