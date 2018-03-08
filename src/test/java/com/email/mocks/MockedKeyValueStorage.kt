package com.email.mocks

import com.email.db.KeyValueStorage

/**
 * Created by gabriel on 3/8/18.
 */
class MockedKeyValueStorage: KeyValueStorage {
    private val stringMap = HashMap<String, String>()
    override fun getString(key: KeyValueStorage.StringKey, default: String): String {
        return stringMap[key.stringKey] ?: default
    }

    override fun putString(key: KeyValueStorage.StringKey, value: String) {
        stringMap[key.stringKey] = value
    }

}