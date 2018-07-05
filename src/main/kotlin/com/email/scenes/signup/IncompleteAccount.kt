package com.email.scenes.signup

import com.email.db.models.Account
import com.email.signal.SignalKeyGenerator

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

        fun complete(privateBundle: SignalKeyGenerator.PrivateBundle, jwt: String) =
                Account(
                        name = this.name,
                        recipientId = this.username,
                        deviceId = this.deviceId,
                        signature = "",
                        jwt = jwt,
                        registrationId = privateBundle.registrationId,
                        identityKeyPairB64 = privateBundle.identityKeyPair
                )
}