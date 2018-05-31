package com.email.scenes.signup.data

import com.email.db.KeyValueStorage
import com.email.db.dao.SignUpDao
import com.email.db.models.Account
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.db.models.signal.CRPreKey
import com.email.db.models.signal.CRSignedPreKey
import com.email.signal.SignalKeyGenerator

/**
 * Encapsulates the transaction needed to atomically persist a new account's data in the database
 * and shared preferences.
 * Created by gabriel on 4/23/18.
 */

class StoreAccountTransaction(private val dao: SignUpDao,
                              private val keyValueStorage: KeyValueStorage) {


    private fun setNewUserAsActiveAccount(user: Account) {
        val activeAccount = ActiveAccount(name = user.name, recipientId = user.recipientId,
                deviceId = user.deviceId, jwt = user.jwt)
        keyValueStorage.putString(KeyValueStorage.StringKey.ActiveAccount,
                activeAccount.toJSON().toString())
    }

    fun run(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle, extraSteps: Runnable?) {
        val preKeyList = keyBundle.preKeys.entries.map { (key, value) ->
            CRPreKey(id = key, byteString = value)
        }
        val signedPreKey = CRSignedPreKey(
                keyBundle.signedPreKeyId,
                keyBundle.signedPreKey)
        val defaultLabels = Label.defaultItems.toList()
        val extraRegistrationSteps = Runnable {
            extraSteps?.run()
            setNewUserAsActiveAccount(account)
        }

        dao.insertNewAccountData(account = account, preKeyList = preKeyList,
                signedPreKey = signedPreKey, defaultLabels = defaultLabels,
                extraRegistrationSteps = extraRegistrationSteps)
    }

    fun run(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle) =
            run(account, keyBundle, null)

}