package com.criptext.mail.splash

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.scenes.signin.SignInActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import io.fabric.sdk.android.Fabric
import java.lang.Exception
import java.lang.ref.WeakReference


/**
 * Created by gesuwall on 3/27/17.
 */

class SplashActivity: AppCompatActivity(), WelcomeTimeout.Listener {

    private var welcomeTimeout: WelcomeTimeout? = null
    private val storage: KeyValueStorage by lazy {
        KeyValueStorage.SharedPrefs(this.applicationContext)
    }
    private val db: AppDatabase by lazy {
        AppDatabase.getAppDatabase(this.applicationContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null && lockManager.appLock.isPasscodeSet && lockManager.isAppLockEnabled
                && storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false)){
            lockManager.appLock.enable()
        }
        cancelNotifications()
        val timeToWait = try {
            if(db.inTransaction())
                WelcomeTimeout.inTransactionTimeout
            else
                WelcomeTimeout.noramlTimeout
        }  catch (ex: Exception) {
            WelcomeTimeout.onErrorAccesingDBTimeout
        }
        welcomeTimeout = WelcomeTimeout(timeToWait, this)
        welcomeTimeout!!.start()
    }

    private fun cancelNotifications(){
        val notificationManager = this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CriptextNotification.INBOX_ID)
        notificationManager.cancel(CriptextNotification.OPEN_ID)
        notificationManager.cancel(CriptextNotification.ERROR_ID)
        notificationManager.cancel(CriptextNotification.LINK_DEVICE_ID)
        storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        storage.putInt(KeyValueStorage.StringKey.SyncNotificationCount, 0)
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

    companion object {
        const val noramlTimeout = 2000L
        const val inTransactionTimeout = 5000L
        const val onErrorAccesingDBTimeout = 8000L
    }
}