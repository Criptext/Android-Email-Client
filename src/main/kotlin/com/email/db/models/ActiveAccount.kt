package com.email.db.models

import android.content.Context
import com.email.api.JSONData
import com.email.db.KeyValueStorage
import org.json.JSONObject

/**
 * Created by gabriel on 3/22/18.
 */

data class ActiveAccount(val name: String, val recipientId: String,
                         val deviceId: Int, val jwt: String, val signature: String) : JSONData {

    val userEmail = "$recipientId@${Contact.mainDomain}"

    override fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("recipientId", recipientId)
        json.put("deviceId", deviceId)
        json.put("jwt", jwt)
        json.put("signature", signature)
        return json
    }

    fun updateFullName(storage: KeyValueStorage, fullName: String){
        val account = toJSON()
        account.put("name", fullName)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateSignature(storage: KeyValueStorage, signature: String){
        val account = toJSON()
        account.put("signature", signature)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    companion object {
        fun fromJSONString(jsonString: String): ActiveAccount {
            val json = JSONObject(jsonString)
            val name = json.getString("name")
            val recipientId = json.getString("recipientId")
            val deviceId = json.getInt("deviceId")
            val jwt = json.getString("jwt")
            val signature = json.getString("signature")

            return ActiveAccount(name = name, recipientId = recipientId, deviceId = deviceId,
                    jwt = jwt, signature = signature)
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