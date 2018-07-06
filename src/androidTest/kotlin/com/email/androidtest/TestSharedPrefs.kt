package com.email.androidtest

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.email.db.KeyValueStorage

/**
 * Created by gabriel on 5/24/18.
 */
class TestSharedPrefs(ctx: Context): KeyValueStorage {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)

    private fun withApply(edit: (SharedPreferences.Editor) -> Unit) {
        val editor = prefs.edit()
        edit(editor)
        editor.apply()
    }

    override fun getLong(key: KeyValueStorage.StringKey, default: Long): Long {
        return prefs.getLong("_test_" + key.stringKey, default)
    }

    override fun putLong(key: KeyValueStorage.StringKey, value: Long) {
        withApply { editor -> editor.putLong("_test_" + key.stringKey, value) }
    }

    override fun getString(key: KeyValueStorage.StringKey, default: String): String =
            prefs.getString("_test_" + key.stringKey, default)

    override fun putString(key: KeyValueStorage.StringKey, value: String) {
        withApply { editor -> editor.putString("_test_" + key.stringKey, value) }
    }

    override fun getStringSet(key: KeyValueStorage.StringKey): MutableSet<String>? {
        return prefs.getStringSet("_test_" + key.stringKey, null)
    }

    override fun putStringSet(key: KeyValueStorage.StringKey, value: MutableSet<String>) {
        withApply { editor -> editor.putStringSet("_test_" + key.stringKey, value) }
    }

    override fun clearAll() {
        withApply { editor -> editor.clear() }
    }
}