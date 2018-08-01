package com.criptext.mail.mocks

import com.criptext.mail.db.KeyValueStorage

/**
 * Created by gabriel on 3/8/18.
 */
class MockedKeyValueStorage: KeyValueStorage {

    override fun getLong(key: KeyValueStorage.StringKey, default: Long): Long {
        return longMap[key.stringKey] ?: default
    }

    override fun putLong(key: KeyValueStorage.StringKey, value: Long) {
        longMap[key.stringKey] = value
    }

    override fun clearAll() {
        stringSetMap.clear()
        stringMap.clear()
    }

    override fun getStringSet(key: KeyValueStorage.StringKey): MutableSet<String>? {
        return stringSetMap[key.stringKey]
    }

    override fun putStringSet(key: KeyValueStorage.StringKey, value: MutableSet<String>) {
        stringSetMap[key.stringKey] = value
    }

    private val stringSetMap = HashMap<String, MutableSet<String>>()
    private val stringMap = HashMap<String, String>()
    private val longMap = HashMap<String, Long>()
    override fun getString(key: KeyValueStorage.StringKey, default: String): String {
        return stringMap[key.stringKey] ?: default
    }

    override fun putString(key: KeyValueStorage.StringKey, value: String) {
        stringMap[key.stringKey] = value
    }

}