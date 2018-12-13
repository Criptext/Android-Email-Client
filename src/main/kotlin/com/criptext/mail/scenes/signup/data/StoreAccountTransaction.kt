package com.criptext.mail.scenes.signup.data

import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.signal.SignalKeyGenerator

/**
 * Encapsulates the transaction needed to atomically persist a new account's data in the database
 * and shared preferences.
 * Created by gabriel on 4/23/18.
 */

class StoreAccountTransaction(private val dao: SignUpDao,
                              private val keyValueStorage: KeyValueStorage) {


    private fun setNewUserAsActiveAccount(user: Account) {
        val activeAccount = ActiveAccount(name = user.name, recipientId = user.recipientId,
                deviceId = user.deviceId, jwt = user.jwt, signature = "", refreshToken = user.refreshToken)
        keyValueStorage.putString(KeyValueStorage.StringKey.ActiveAccount,
                activeAccount.toJSON().toString())
    }

    fun run(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle, extraSteps: Runnable?, keepData: Boolean = false) {
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

        if(!keepData) {
            dao.insertNewAccountData(account = account, preKeyList = preKeyList,
                    signedPreKey = signedPreKey, defaultLabels = defaultLabels,
                    extraRegistrationSteps = extraRegistrationSteps)
        }else{
            dao.updateAccountData(account = account, preKeyList = preKeyList, signedPreKey = signedPreKey,
                    extraRegistrationSteps = extraRegistrationSteps)
        }
    }

    fun run(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle) =
            run(account, keyBundle, null)

}