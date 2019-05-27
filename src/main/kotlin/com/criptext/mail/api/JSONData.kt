package com.criptext.mail.api

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by gabriel on 3/21/18.
 */

interface JSONData {
    fun toJSON(): JSONObject

    fun List<JSONData>.toJSONArray(): JSONArray {
        val jsonArray = JSONArray()
        this.forEach { jsonArray.put(it.toJSON()) }
        return jsonArray
    }


}

fun List<Long>.toJSONLongArray():
        JSONArray {
        val jsonArray = JSONArray()
        this.forEach { jsonArray.put(it) }
        return jsonArray
    }

@Suppress("UNCHECKED_CAST")
fun <T> JSONArray.toList(): List<T> {
    val list = mutableListOf<T>()
    for(i in 0 until this.length()) list.add(this[i] as T)
    return list
}

fun <T> JSONArray.toMutableList(): MutableList<T> {
    val list = mutableListOf<T>()
    for(i in 0 until this.length()) list.add(this[i] as T)
    return list
}
