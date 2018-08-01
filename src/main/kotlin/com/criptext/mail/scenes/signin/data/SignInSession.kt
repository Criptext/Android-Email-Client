package com.criptext.mail.scenes.signin.data

import org.json.JSONObject

/**
 * Created by gabriel on 5/17/18.
 */
data class SignInSession(val token: String, val deviceId: Int, val name: String) {

    fun toJSON():JSONObject {
        val json = JSONObject()
        json.put("token", token)
        json.put("deviceId", deviceId)
        json.put("name", name)
        return json
    }

    companion object {
        fun fromJSON(jsonObject: JSONObject): SignInSession = SignInSession(
                token = jsonObject.getString("token"),
                deviceId = jsonObject.getInt("deviceId"),
                name = jsonObject.getString("name")
        )
    }
}