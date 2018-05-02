package com.email

import android.app.Application
import com.email.db.models.ActiveAccount
import com.email.websocket.WebSocketSingleton
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
    }
}
