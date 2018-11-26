package com.criptext.mail

import android.support.multidex.MultiDexApplication
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.linking.LinkingActivity
import com.criptext.mail.scenes.settings.pinlock.pinscreen.LockScreenActivity
import com.criptext.mail.scenes.signin.SignInActivity
import com.criptext.mail.scenes.signup.SignUpActivity
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.websocket.WebSocketSingleton
import com.facebook.stetho.Stetho
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
            WebSocketSingleton.getInstance(activeAccount)
        }
    }
}
