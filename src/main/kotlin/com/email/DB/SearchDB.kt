package com.email.DB

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by danieltigse on 2/5/18.
 */

interface SearchLocalDB{

    fun saveHistorySearch(value: String)
    fun getHistorySearch(): List<String>
    fun seed()

    class Default(applicationContext: Context): SearchLocalDB{

        private val prefs : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        override fun saveHistorySearch(value: String) {
            val list = prefs.getStringSet("searchHistory", null) ?: mutableSetOf()
            list.add(value)
            prefs.edit().putStringSet("searchHistory", list).apply()
        }

        override fun getHistorySearch(): List<String> {
            val set = prefs.getStringSet("searchHistory", null)
            return set.toList()
        }

        override fun seed() {
            saveHistorySearch("Catalina Solis")
            saveHistorySearch("Important meeting")
            saveHistorySearch("Lion")
            saveHistorySearch("Dogs playing")
            saveHistorySearch("Iron Maden Concert")
        }

    }

}