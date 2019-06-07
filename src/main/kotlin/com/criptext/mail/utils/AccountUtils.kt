package com.criptext.mail.utils

import com.criptext.mail.api.toList
import com.criptext.mail.api.toMutableList
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.settings.privacy.data.ExternalEncryptionData
import org.json.JSONArray
import org.json.JSONObject

object AccountUtils {
    fun setUserAsActiveAccount(user: Account, storage: KeyValueStorage): ActiveAccount{
        val activeAccount = ActiveAccount(id = user.id, name = user.name, recipientId = user.recipientId,
                deviceId = user.deviceId, jwt = user.jwt, signature = "", refreshToken = user.refreshToken,
                domain = user.domain)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount,
                activeAccount.toJSON().toString())
        return activeAccount
    }

    fun getLastLoggedAccounts(storage: KeyValueStorage): MutableList<String> {
        val storageLastUser = storage.getString(KeyValueStorage.StringKey.LastLoggedUser, "")
        return if(storageLastUser.isEmpty()) mutableListOf() else
            storageLastUser.split(",").map { it.trim() }.toMutableList()
    }

    fun hasExternalEncryption(storage: KeyValueStorage, email: String): Boolean{
        val stringStorage = storage.getString(KeyValueStorage.StringKey.EncryptToExternals, "")
        if(stringStorage.isEmpty()) return false
        val data = ExternalEncryptionData.fromJson(stringStorage)
        val find = data.find { it.email == email }
        if(find != null) return find.hasEncryption
        return false
    }

    fun saveExternalEncryptionSetting(storage: KeyValueStorage, email: String, hasEncryption: Boolean) {
        val stringStorage = storage.getString(KeyValueStorage.StringKey.EncryptToExternals, "")
        val data = if(stringStorage.isNotEmpty()) ExternalEncryptionData.fromJson(stringStorage) else mutableListOf()
        data.remove(data.find { it.email == email })
        data.add(ExternalEncryptionData(
                email = email,
                hasEncryption = hasEncryption
        ))
        storage.putString(KeyValueStorage.StringKey.EncryptToExternals, ExternalEncryptionData.toJSON(data).toString())
    }
}