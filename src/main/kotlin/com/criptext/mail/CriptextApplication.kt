package com.criptext.mail

import android.R
import androidx.multidex.MultiDexApplication
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.pinlock.pinscreen.LockScreenActivity
import com.criptext.mail.scenes.signin.SignInActivity
import com.criptext.mail.scenes.signup.SignUpActivity
import com.criptext.mail.services.jobs.CriptextJobCreator
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.websocket.WebSocketSingleton
import com.evernote.android.job.JobManager
import com.facebook.stetho.Stetho
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump


class CriptextApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/NunitoSans-Regular.ttf")
                                .build()))
                .build())
        if(BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
        val activeAccount = ActiveAccount.loadFromStorage(applicationContext)
        JobManager.create(this).addJobCreator(CriptextJobCreator())
        if(activeAccount != null) {
            val storage = KeyValueStorage.SharedPrefs(this)
            val lockManager = LockManager.getInstance()
            if(storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false)) {
                lockManager.enableAppLock(this, LockScreenActivity::class.java)
                configureAppLock(lockManager)
            }
            val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
            if(jwts.isNotEmpty())
                WebSocketSingleton.getInstance(jwts)
            else
                WebSocketSingleton.getInstance(activeAccount.jwt)
        }
    }

    private fun configureAppLock(lockManager: LockManager<AppLockActivity>){
        lockManager.appLock.setOnlyBackgroundTimeout(true)
        lockManager.appLock.addIgnoredActivity(SplashActivity::class.java)
        lockManager.appLock.addIgnoredActivity(SignInActivity::class.java)
        lockManager.appLock.addIgnoredActivity(SignUpActivity::class.java)
    }
}
