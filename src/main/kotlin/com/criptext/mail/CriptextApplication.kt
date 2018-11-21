package com.criptext.mail

import android.support.multidex.MultiDexApplication
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.websocket.WebSocketSingleton
import com.facebook.stetho.Stetho
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class CriptextApplication : MultiDexApplication(), LifecycleDelegate{

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
            val lifeCycleHandler = AppLifecycleHandler(this)
            registerLifecycleHandler(lifeCycleHandler)
        }
    }

    override fun onAppBackgrounded() {
        val storage = KeyValueStorage.SharedPrefs(this)
        storage.putBool(KeyValueStorage.StringKey.AskForPin, true)
    }

    override fun onAppForegrounded() {

    }

    private fun registerLifecycleHandler(lifeCycleHandler: AppLifecycleHandler) {
        registerActivityLifecycleCallbacks(lifeCycleHandler)
        registerComponentCallbacks(lifeCycleHandler)
    }

}
