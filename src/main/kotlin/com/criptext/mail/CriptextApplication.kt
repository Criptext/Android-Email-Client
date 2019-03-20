package com.criptext.mail

import android.os.AsyncTask
import androidx.multidex.MultiDexApplication
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.pinlock.pinscreen.LockScreenActivity
import com.criptext.mail.scenes.signin.SignInActivity
import com.criptext.mail.scenes.signup.SignUpActivity
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.websocket.WebSocketSingleton
import com.facebook.stetho.Stetho
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import com.github.omadahealth.lollipin.lib.managers.LockManager



class CriptextApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NunitoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        if(BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
        val activeAccount = ActiveAccount.loadFromStorage(applicationContext)
        if(activeAccount != null) {
            val storage = KeyValueStorage.SharedPrefs(this)
            val lockManager = LockManager.getInstance()
            if(storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false)) {
                lockManager.enableAppLock(this, LockScreenActivity::class.java)
                configureAppLock(lockManager)
            }
            WebSocketSingleton.getInstance(activeAccount)
        }
    }

    private fun configureAppLock(lockManager: LockManager<AppLockActivity>){
        lockManager.appLock.setOnlyBackgroundTimeout(true)
        lockManager.appLock.addIgnoredActivity(SplashActivity::class.java)
        lockManager.appLock.addIgnoredActivity(SignInActivity::class.java)
        lockManager.appLock.addIgnoredActivity(SignUpActivity::class.java)
    }
}
