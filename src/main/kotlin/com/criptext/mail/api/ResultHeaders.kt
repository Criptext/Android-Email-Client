package com.criptext.mail.api

import okhttp3.Headers
import java.util.*

class ResultHeaders(private val headers: Headers){

    fun getString(name: String):String {
        return headers.get(name).toString()
    }

    fun getInt(name: String):Int {
        return headers.get(name)?.toInt() ?: 0
    }

    fun getLong(name: String):Long {
        return headers.get(name)?.toLong() ?: 0
    }

}