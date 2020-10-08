package com.criptext.mail.db.dao

import android.os.IBinder
import androidx.room.*
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.Label
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSignedPreKey

/**
 * Data Access Object to be used exclusively by the Sign Up Scene.
 * Created by gabriel on 4/23/18.
 */
@Dao
interface SignUpDao {

    @Insert
    fun insertLabels(labels: List<Label>)

    @Insert
    fun saveAccount(account : Account)

    @Query("""UPDATE account
        SET name=:name,
        jwt=:jwt,
        deviceId=:deviceId,
        refreshToken=:refreshJwt,
        identityKeyPairB64=:identityKey,
        registrationId=:registrationId,
        domain=:domain,
        isActive=:isActive,
        isLoggedIn=:isLoggedIn,
        hasCloudBackup=:hasCloudBackup,
        wifiOnly=:wifiOnly,
        autoBackupFrequency=:backupFrequency,
        type=:type,
        blockRemoteContent=:blockedRemoteContent
        WHERE recipientId=:recipientId
    """)
    fun updateAccount(recipientId: String, name: String, jwt: String, refreshJwt: String,
                      deviceId: Int, identityKey: String, registrationId: Int, domain: String,
                      isActive: Int, isLoggedIn: Int, hasCloudBackup: Boolean, wifiOnly: Boolean,
                      backupFrequency: Int, type: AccountTypes, blockedRemoteContent: Boolean)

    @Insert
    fun insertPreKeys(preKeys : List<CRPreKey>)

    @Insert
    fun insertSignedPreKey(crSignedPreKey: CRSignedPreKey)

    @Transaction
    fun insertNewAccountData(account: Account, preKeyList: List<CRPreKey>,
                             signedPreKey: CRSignedPreKey, defaultLabels: List<Label>,
                             extraRegistrationSteps: Runnable, accountDao: AccountDao,
                             isMultiple: Boolean) {
        saveAccount(account)
        val savedAccount = accountDao.getLoggedInAccount()!!
        preKeyList.forEach { it.accountId = savedAccount.id }
        insertPreKeys(preKeyList)
        signedPreKey.accountId = savedAccount.id
        insertSignedPreKey(signedPreKey)

        if(!isMultiple)
            insertLabels(defaultLabels)
        // execute extra steps here, so that if they fail, we can rollback
        extraRegistrationSteps.run()
    }

    @Transaction
    fun updateAccountData(account: Account, preKeyList: List<CRPreKey>,
                             signedPreKey: CRSignedPreKey,
                             extraRegistrationSteps: Runnable, accountDao: AccountDao,
                          isMultiple: Boolean = false) {
        updateAccount(recipientId = account.recipientId, name = account.name, deviceId = account.deviceId,
                domain = account.domain, isLoggedIn = if(account.isLoggedIn) 1 else 0, isActive = if(account.isActive) 1 else 0,
                identityKey = account.identityKeyPairB64, jwt = account.jwt, refreshJwt = account.refreshToken,
                registrationId = account.registrationId, backupFrequency = account.autoBackupFrequency, hasCloudBackup = account.hasCloudBackup,
                wifiOnly = account.wifiOnly, type = account.type, blockedRemoteContent = account.blockRemoteContent)

        val savedAccount = accountDao.getLoggedInAccount()!!
        preKeyList.forEach { it.accountId = savedAccount.id }
        insertPreKeys(preKeyList)
        signedPreKey.accountId = savedAccount.id
        insertSignedPreKey(signedPreKey)
        // execute extra steps here, so that if they fail, we can rollback
        extraRegistrationSteps.run()
    }

}