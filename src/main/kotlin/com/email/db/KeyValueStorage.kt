package com.email.db

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by gabriel on 3/8/18.
 */

interface KeyValueStorage {

    fun getString(key: StringKey, default: String): String
    fun putString(key: StringKey, value: String)
    fun getStringSet(key: StringKey): MutableSet<String>?
    fun putStringSet(key: StringKey, value: MutableSet<String>)
    fun clearAll()

    enum class StringKey(val stringKey: String) {
        ActiveAccount("ActiveAccount"), SignInSession("SignInSession"),
        SearchHistory("searchHistory")
    }

    class SharedPrefs(ctx: Context) : KeyValueStorage {
        private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)

        private fun withApply(edit: (SharedPreferences.Editor) -> Unit) {
            val editor = prefs.edit()
            edit(editor)
            editor.apply()
        }

        override fun getString(key: StringKey, default: String): String =
                prefs.getString(key.stringKey, default)

        override fun putString(key: StringKey, value: String) {
            withApply { editor -> editor.putString(key.stringKey, value) }
        }

        override fun getStringSet(key: StringKey): MutableSet<String>? {
            return prefs.getStringSet(key.stringKey, null)
        }

        override fun putStringSet(key: StringKey, value: MutableSet<String>) {
            withApply { editor -> editor.putStringSet(key.stringKey, value) }
        }

        override fun clearAll() {
            withApply { editor -> editor.clear() }
        }
    }
}
