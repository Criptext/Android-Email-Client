package com.email.db.models

import android.content.Context
import com.email.api.JSONData
import com.email.db.KeyValueStorage
import org.json.JSONObject

/**
 * Created by gabriel on 3/22/18.
 */

data class ActiveAccount(val name: String, val recipientId: String, val deviceId: Int, val jwt: String) : JSONData {

    val userEmail = "$recipientId@${Contact.mainDomain}"

    override fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("recipientId", recipientId)
        json.put("deviceId", deviceId)
        json.put("jwt", jwt)
        return json
    }

    companion object {
        fun fromJSONString(jsonString: String): ActiveAccount {
            val json = JSONObject(jsonString)
            val name = json.getString("name")
            val recipientId = json.getString("recipientId")
            val deviceId = json.getInt("deviceId")
            val jwt = json.getString("jwt")

            return ActiveAccount(name = name, recipientId = recipientId, deviceId = deviceId,
                    jwt = jwt)
        }

        fun loadFromStorage(storage: KeyValueStorage): ActiveAccount? {
            val jsonString = storage.getString(KeyValueStorage.StringKey.ActiveAccount, "")
            if (jsonString.isEmpty())
                return null
            return fromJSONString(jsonString)
        }

        fun loadFromStorage(context: Context): ActiveAccount? =
                loadFromStorage(KeyValueStorage.SharedPrefs(context))
    }



}