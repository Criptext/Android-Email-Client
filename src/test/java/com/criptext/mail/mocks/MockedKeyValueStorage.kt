package com.criptext.mail.mocks

import com.criptext.mail.db.KeyValueStorage

/**
 * Created by gabriel on 3/8/18.
 */
class MockedKeyValueStorage: KeyValueStorage {
    override fun remove(keyList: List<KeyValueStorage.StringKey>) {
        keyList.forEach {
            boolMap.remove(it.stringKey)
            intMap.remove(it.stringKey)
            longMap.remove(it.stringKey)
            stringSetMap.remove(it.stringKey)
            stringMap.remove(it.stringKey)
        }
    }

    override fun getBool(key: KeyValueStorage.StringKey, default: Boolean): Boolean {
        return boolMap[key.stringKey] ?: default
    }

    override fun putBool(key: KeyValueStorage.StringKey, value: Boolean) {
        boolMap[key.stringKey] = value
    }

    override fun getInt(key: KeyValueStorage.StringKey, default: Int): Int {
        return intMap[key.stringKey] ?: default
    }

    override fun putInt(key: KeyValueStorage.StringKey, value: Int) {
        intMap[key.stringKey] = value
    }

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
    private val intMap = HashMap<String, Int>()
    private val boolMap = HashMap<String, Boolean>()
    override fun getString(key: KeyValueStorage.StringKey, default: String): String {
        return stringMap[key.stringKey] ?: default
    }

    override fun getString(file: String, key: KeyValueStorage.StringKey, default: String): String {
        return getString(key, default)
    }

    override fun putString(key: KeyValueStorage.StringKey, value: String) {
        stringMap[key.stringKey] = value
    }

    override fun putString(file: String, key: KeyValueStorage.StringKey, value: String) {
        putString(key, value)
    }

}