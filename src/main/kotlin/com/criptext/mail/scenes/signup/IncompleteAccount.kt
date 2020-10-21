package com.criptext.mail.scenes.signup

import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.Contact
import com.criptext.mail.signal.SignalKeyGenerator

/**
 * Created by sebas on 3/7/18.
 */

data class IncompleteAccount(
        val username: String,
        val deviceId: Int,
        val name: String,
        val password: String,
        val recoveryEmail: String?,
        val captchaKey: String,
        val captchaAnswer: String
        ) {

        fun complete(privateBundle: SignalKeyGenerator.PrivateBundle, jwt: String, refreshToken: String) =
                Account(
                        id = 0,
                        name = this.name,
                        recipientId = this.username,
                        deviceId = this.deviceId,
                        signature = "",
                        jwt = jwt,
                        refreshToken = refreshToken,
                        registrationId = privateBundle.registrationId,
                        identityKeyPairB64 = privateBundle.identityKeyPair,
                        domain = Contact.mainDomain,
                        isActive = true,
                        isLoggedIn = true,
                        hasCloudBackup = false,
                        lastTimeBackup = null,
                        autoBackupFrequency = 0,
                        wifiOnly = true,
                        backupPassword = null,
                        type = AccountTypes.STANDARD,
                        blockRemoteContent = true,
                        defaultAddress = null
                )
}