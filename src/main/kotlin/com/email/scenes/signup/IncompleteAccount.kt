package com.email.scenes.signup

import com.email.db.models.Account
import com.email.signal.SignalKeyGenerator

/**
 * Created by sebas on 3/7/18.
 */

data class IncompleteAccount(
        val username: String,
        val name: String,
        val password: String,
        val recoveryEmail: String?
        ) {

        fun complete(privateBundle: SignalKeyGenerator.PrivateBundle, jwt: String) =
                Account(
                        name = this.name,
                        recipientId = this.username,
                        jwt = jwt,
                        registrationId = privateBundle.registrationId,
                        identityB64 = privateBundle.identityKeyPair
                )
}