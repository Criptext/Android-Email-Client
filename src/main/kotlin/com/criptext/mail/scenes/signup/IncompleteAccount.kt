package com.criptext.mail.scenes.signup

import com.criptext.mail.db.models.Account
import com.criptext.mail.signal.SignalKeyGenerator

/**
 * Created by sebas on 3/7/18.
 */

data class IncompleteAccount(
        val username: String,
        val deviceId: Int,
        val name: String,
        val password: String,
        val recoveryEmail: String?
        ) {

        fun complete(privateBundle: SignalKeyGenerator.PrivateBundle, jwt: String, refreshToken: String) =
                Account(
                        name = this.name,
                        recipientId = this.username,
                        deviceId = this.deviceId,
                        signature = "",
                        jwt = jwt,
                        refreshToken = refreshToken,
                        registrationId = privateBundle.registrationId,
                        identityKeyPairB64 = privateBundle.identityKeyPair
                )
}