package com.criptext.mail.utils

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by gabriel on 5/8/18.
 */

class JSONArrayHolder(private val array: JSONArray) {
    fun ofLength(length: Int) = array.length() == length
    fun match() = true
}

fun JSONObject.hasArray(name: String): JSONArrayHolder {
    val array = this.getJSONArray(name)
    return JSONArrayHolder(array)
}