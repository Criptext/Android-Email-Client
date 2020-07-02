package com.criptext.mail.db.models

import android.content.Context
import com.criptext.mail.api.JSONData
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.typeConverters.AccountTypeConverter
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.generaldatasource.workers.ChangeBlockRemoteContentSettingWorker
import org.json.JSONObject

/**
 * Created by gabriel on 3/22/18.
 */

data class ActiveAccount(val id: Long, val name: String, val recipientId: String, val domain: String,
                         val deviceId: Int, val jwt: String, val refreshToken: String, val signature: String,
                         val type: AccountTypes, val blockRemoteContent: Boolean, val defaultAddress: Long?) : JSONData {

    val userEmail = "$recipientId@$domain"

    override fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("recipientId", recipientId)
        json.put("domain", domain)
        json.put("deviceId", deviceId)
        json.put("jwt", jwt)
        json.put("refreshToken", refreshToken)
        json.put("signature", signature)
        json.put("type", type.ordinal)
        json.put("blockRemoteContent", blockRemoteContent)
        if(defaultAddress != null) json.put("defaultAddress", defaultAddress)
        return json
    }

    fun updateFullName(storage: KeyValueStorage, fullName: String){
        val account = toJSON()
        account.put("name", fullName)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateAccountId(storage: KeyValueStorage, accountId: Long){
        val account = toJSON()
        account.put("id", accountId)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateSignature(storage: KeyValueStorage, signature: String){
        val account = toJSON()
        account.put("signature", signature)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateUserWithSessionData(storage: KeyValueStorage, sessionData: String){
        val account = toJSON()
        account.put("jwt", sessionData)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateUserWithTokensData(storage: KeyValueStorage, sessionData: String){
        val account = toJSON()
        account.put("jwt", JSONObject(sessionData).get("token"))
        account.put("refreshToken", JSONObject(sessionData).get("refreshToken"))
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateAccountType(storage: KeyValueStorage, newType: AccountTypes){
        val account = toJSON()
        account.put("type", newType.ordinal)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateAccountBlockedRemoteContent(storage: KeyValueStorage, newBlockRemoteContent: Boolean){
        val account = toJSON()
        account.put("blockRemoteContent", newBlockRemoteContent)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
    }

    fun updateAccountDefaultAddress(storage: KeyValueStorage, newDefaultAddress: Long?){
        val account = toJSON()
        if(newDefaultAddress != null) {
            account.put("defaultAddress", newDefaultAddress)
            storage.putString(KeyValueStorage.StringKey.ActiveAccount, account.toString())
        }
    }

    companion object {
        fun fromJSONString(jsonString: String): ActiveAccount {
            val json = JSONObject(jsonString)
            val name = json.getString("name")
            val id = json.optLong("id")
            val recipientId = json.getString("recipientId")
            val deviceId = json.getInt("deviceId")
            val jwt = json.getString("jwt")
            val refreshToken = json.optString("refreshToken")
            val signature = json.getString("signature")
            val domain = if(json.has("domain")) json.getString("domain") else Contact.mainDomain
            val type = if(json.has("type")) json.getInt("type") else 0
            val blockRemoteContent = if(json.has("blockRemoteContent")) json.getBoolean("blockRemoteContent") else true
            val defaultAddress = if(json.has("defaultAddress")) json.getLong("defaultAddress") else null

            return ActiveAccount(id = if(id == 0L) 1 else id, name = name, recipientId = recipientId, deviceId = deviceId,
                    jwt = jwt, signature = signature, refreshToken = refreshToken, domain = domain,
                    type = AccountUtils.getAccountTypeFromInt(type), blockRemoteContent = blockRemoteContent, defaultAddress = defaultAddress)
        }

        fun loadFromDB(account: Account): ActiveAccount? {
            return ActiveAccount(id= account.id, name = account.name, recipientId = account.recipientId, deviceId = account.deviceId,
                    jwt = account.jwt, signature = account.signature, refreshToken = account.refreshToken,
                    domain = account.domain, type = account.type, blockRemoteContent = account.blockRemoteContent,
                    defaultAddress = account.defaultAddress)
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