package com.email.mocks

import com.email.db.KeyValueStorage

/**
 * Created by gabriel on 3/8/18.
 */
class MockedKeyValueStorage: KeyValueStorage {

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
    override fun getString(key: KeyValueStorage.StringKey, default: String): String {
        return stringMap[key.stringKey] ?: default
    }

    override fun putString(key: KeyValueStorage.StringKey, value: String) {
        stringMap[key.stringKey] = value
    }

}