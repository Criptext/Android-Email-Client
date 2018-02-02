package com.email

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class DeckardApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NunitoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}
