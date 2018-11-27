package com.criptext.mail.splash

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.scenes.signin.SignInActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import io.fabric.sdk.android.Fabric
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
        Fabric.with(this, Crashlytics())
        val notificationManager = this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null && lockManager.appLock.isPasscodeSet && lockManager.isAppLockEnabled
                && storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false)){
            lockManager.appLock.enable()
        }
        notificationManager.cancelAll()
        storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        welcomeTimeout = WelcomeTimeout(2000L, this)
        welcomeTimeout!!.start()

    }

    private fun hasActiveAccount(): Boolean =
        storage.getString(KeyValueStorage.StringKey.ActiveAccount, "") != ""

    override fun onTimeout() {
        finish()

        if(hasActiveAccount()){
            val mailBoxIntent = Intent(this, MailboxActivity::class.java)
            startActivity(mailBoxIntent)
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