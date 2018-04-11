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

    enum class StringKey(val stringKey: String) {
        ActiveAccount("ActiveAccount")
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

    }
}
