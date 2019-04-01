package com.criptext.mail.utils

import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount

object AccountUtils {
    fun setUserAsActiveAccount(user: Account, storage: KeyValueStorage): ActiveAccount{
        val activeAccount = ActiveAccount(id = user.id, name = user.name, recipientId = user.recipientId,
                deviceId = user.deviceId, jwt = user.jwt, signature = "", refreshToken = user.refreshToken)
        storage.putString(KeyValueStorage.StringKey.ActiveAccount,
                activeAccount.toJSON().toString())
        return activeAccount
    }

    fun getLastLoggedAccounts(storage: KeyValueStorage): MutableList<String> {
        val storageLastUser = storage.getString(KeyValueStorage.StringKey.LastLoggedUser, "")
        return if(storageLastUser.isEmpty()) mutableListOf() else
            storageLastUser.split(",").map { it.trim() }.toMutableList()
    }
}