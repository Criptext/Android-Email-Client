package com.email.activity

import android.app.Activity
import android.os.Bundle
import com.email.DB.AppDatabase
import com.email.DB.seeders.EmailLabelSeeder
import com.email.DB.seeders.EmailSeeder
import com.email.DB.seeders.LabelSeeder
import com.email.R

class DeckardActivity : Activity() {
    var db : AppDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.deckard)
        db = AppDatabase.getAppDatabase(this.applicationContext)
        LabelSeeder.seed(db!!.labelDao())
        EmailSeeder.seed(db!!.emailDao())
        EmailLabelSeeder.seed(db!!.emailLabelDao())
    }
}
