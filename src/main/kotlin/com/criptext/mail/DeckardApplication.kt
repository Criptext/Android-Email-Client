package com.criptext.mail

import android.app.Application
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.websocket.WebSocketSingleton
import com.facebook.stetho.Stetho
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class DeckardApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NunitoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        Stetho.initializeWithDefaults(this)
        val activeAccount = ActiveAccount.loadFromStorage(applicationContext)
        if(activeAccount != null) {
            WebSocketSingleton.getInstance(activeAccount, applicationContext)
        }
        appKeyStorage = KeyValueStorage.SharedPrefs(applicationContext)
    }

    companion object {

        private lateinit var appKeyStorage: KeyValueStorage

        fun getAppStorage(): KeyValueStorage {
            return appKeyStorage
        }
    }

}
