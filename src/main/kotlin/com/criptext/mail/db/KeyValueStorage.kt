package com.criptext.mail.db

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by gabriel on 3/8/18.
 */

interface KeyValueStorage {

    fun getString(key: StringKey, default: String): String
    fun getString(file: String, key: StringKey, default: String): String
    fun putString(key: StringKey, value: String)
    fun putString(file: String, key: StringKey, value: String)
    fun getStringSet(key: StringKey): MutableSet<String>?
    fun putStringSet(key: StringKey, value: MutableSet<String>)
    fun getLong(key: StringKey, default: Long): Long
    fun putLong(key: StringKey, value: Long)
    fun getInt(key: StringKey, default: Int): Int
    fun putInt(key: StringKey, value: Int)
    fun getBool(key: StringKey, default: Boolean): Boolean
    fun putBool(key: StringKey, value: Boolean)
    fun clearAll()

    enum class StringKey(val stringKey: String) {
        ActiveAccount("ActiveAccount"), SignInSession("SignInSession"),
        SearchHistory("searchHistory"), LastTimeFeedOpened("LastTimeFeedOpened"),
        LastTimeConfirmationLinkSent("LastTimeConfirmationLinkSent"),
        LastLoggedUser("LastLoggedUser"), NewMailNotificationCount("NewMailPushCount"),
        ShowSyncPhonebookDialog("ShowSyncPhonebookDialog"),
        ShowEmailPreview("ShowEmailPreview"), HasLockPinActive("HasLockPinActive"),
        PIN("pin"), AskForPin("AskForPin"), PINTimeout("PINTimeout"),
        UserHasAcceptedPhonebookSync("UserHasAcceptedPhonebookSync"),
        StartGuideShowEmail("StartGuideShowEmail"),
        StartGuideShowAttachments("StartGuideShowAttachments"),
        StartGuideShowMultiple("StartGuideShowMultiple"),
        StartGuideShowNotification("StartGuideShowNotification"),
        StartGuideShowOptions("StartGuideShowOptions"),
        StartGuideShowEmailRead("StartGuideShowEmailRead"),
    }

    class SharedPrefs(private val ctx: Context) : KeyValueStorage {

        private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)

        private fun withApply(edit: (SharedPreferences.Editor) -> Unit) {
            val editor = prefs.edit()
            edit(editor)
            editor.apply()
        }

        override fun getString(key: StringKey, default: String): String =
                prefs.getString(key.stringKey, default)

        override fun getString(file: String, key: StringKey, default: String): String  {
            val mPrefs = ctx.getSharedPreferences(file, Context.MODE_PRIVATE)
            return mPrefs.getString(key.stringKey, default)
        }

        override fun putString(key: StringKey, value: String) {
            withApply { editor -> editor.putString(key.stringKey, value) }
        }

        override fun putString(file: String, key: StringKey, value: String) {
            val mPrefs = ctx.getSharedPreferences(file, Context.MODE_PRIVATE)
            mPrefs.edit().putString(key.stringKey, value).apply()
        }

        override fun getStringSet(key: StringKey): MutableSet<String>? {
            return prefs.getStringSet(key.stringKey, null)
        }

        override fun putStringSet(key: StringKey, value: MutableSet<String>) {
            withApply { editor -> editor.putStringSet(key.stringKey, value) }
        }

        override fun getLong(key: StringKey, default: Long): Long {
            return prefs.getLong(key.stringKey, default)
        }

        override fun putLong(key: StringKey, value: Long) {
            withApply { editor -> editor.putLong(key.stringKey, value) }
        }

        override fun getInt(key: StringKey, default: Int): Int {
            return prefs.getInt(key.stringKey, default)
        }

        override fun putInt(key: StringKey, value: Int) {
            withApply { editor -> editor.putInt(key.stringKey, value) }
        }

        override fun getBool(key: StringKey, default: Boolean): Boolean {
            return prefs.getBoolean(key.stringKey, default)
        }

        override fun putBool(key: StringKey, value: Boolean) {
            withApply { editor -> editor.putBoolean(key.stringKey, value) }
        }

        override fun clearAll() {
            withApply { editor -> editor.clear() }
        }
    }
}
