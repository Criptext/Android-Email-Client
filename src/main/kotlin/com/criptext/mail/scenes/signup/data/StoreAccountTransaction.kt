package com.criptext.mail.scenes.signup.data

import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.AliasDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.*
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.scenes.settings.data.AliasData
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.signal.SignalKeyGenerator
import org.json.JSONArray

/**
 * Encapsulates the transaction needed to atomically persist a new account's data in the database
 * and shared preferences.
 * Created by gabriel on 4/23/18.
 */

class StoreAccountTransaction(private val dao: SignUpDao,
                              private val keyValueStorage: KeyValueStorage,
                              private val accountDao: AccountDao,
                              private val aliasDao: AliasDao,
                              private val customDomainDao: CustomDomainDao) {


    private fun setNewUserAsActiveAccount(user: Account) {
        val activeAccount = ActiveAccount(id = user.id, name = user.name, recipientId = user.recipientId,
                deviceId = user.deviceId, jwt = user.jwt, signature = "", refreshToken = user.refreshToken,
                domain = user.domain, type = user.type, blockRemoteContent = user.blockRemoteContent,
                defaultAddress = user.defaultAddress)
        keyValueStorage.putString(KeyValueStorage.StringKey.ActiveAccount,
                activeAccount.toJSON().toString())
    }


    fun run(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle, extraSteps: Runnable?,
            keepData: Boolean = false, isMultiple: Boolean = false, addressesJsonArray: JSONArray? = null) {

        accountDao.updateActiveInAccount()
        val preKeyList = keyBundle.preKeys.entries.map { (key, value) ->
            CRPreKey(id = 0, preKeyId = key, byteString = value, accountId = account.id)
        }
        val signedPreKey = CRSignedPreKey(
                keyBundle.signedPreKeyId,
                keyBundle.signedPreKey, account.id)
        val defaultLabels = Label.defaultItems.toList()
        val extraRegistrationSteps = Runnable {
            extraSteps?.run()
            val dbAccount = accountDao.getLoggedInAccount()!!
            if(addressesJsonArray != null){
                val aliasDataAndDomains = AliasData.fromJSONArray(addressesJsonArray, dbAccount.id)
                if(aliasDataAndDomains.second.isNotEmpty()) {
                    aliasDao.insertAll(aliasDataAndDomains.second.map {
                        Alias(
                                id = 0,
                                name = it.name,
                                active = it.isActive,
                                domain = if (it.domain == Contact.mainDomain) null else it.domain,
                                rowId = it.rowId,
                                accountId = dbAccount.id
                        )
                    })
                    val defaultAlias = aliasDataAndDomains.second.findLast { it.isDefault }
                    if(defaultAlias != null) {
                        accountDao.updateDefaultAddress(dbAccount.recipientId, dbAccount.domain,
                                defaultAlias.rowId)
                        dbAccount.defaultAddress = defaultAlias.rowId
                    }
                }
                if(aliasDataAndDomains.first.isNotEmpty()) {
                    customDomainDao.insertAll(aliasDataAndDomains.first.filter { it.name != Contact.mainDomain }.map {
                        CustomDomain(
                                id = 0,
                                name = it.name,
                                validated = it.validated,
                                accountId = dbAccount.id
                        )
                    })
                }
            }
            setNewUserAsActiveAccount(dbAccount)
            if(dbAccount.hasCloudBackup){
                CloudBackupJobService.scheduleJob(keyValueStorage, dbAccount)
            }
        }

        if(!keepData) {
            dao.insertNewAccountData(account = account, preKeyList = preKeyList,
                    signedPreKey = signedPreKey, defaultLabels = defaultLabels,
                    extraRegistrationSteps = extraRegistrationSteps, accountDao = accountDao,
                    isMultiple = isMultiple)

        }else{
            dao.updateAccountData(account = account, preKeyList = preKeyList, signedPreKey = signedPreKey,
                    extraRegistrationSteps = extraRegistrationSteps, accountDao = accountDao)
        }
    }

    fun run(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle, isMultiple: Boolean = false) =
            run(account, keyBundle, null, isMultiple = isMultiple)

}
