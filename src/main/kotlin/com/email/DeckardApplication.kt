package com.email

import android.app.Application
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
        Thread.setDefaultUncaughtExceptionHandler(object: Thread.UncaughtExceptionHandler {
            override fun uncaughtException(thread: Thread?, throwable: Throwable?) {
                throwable?.printStackTrace()
            }
        })
    }
}
