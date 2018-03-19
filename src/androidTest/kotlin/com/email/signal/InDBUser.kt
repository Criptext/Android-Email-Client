package com.email.signal

import com.email.db.AppDatabase
import com.email.db.SignUpLocalDB
import com.email.scenes.signup.IncompleteAccount
import org.whispersystems.libsignal.state.SignalProtocolStore

/**
 * Created by gabriel on 3/17/18.
 */

class InDBUser(private val db: AppDatabase, signUpLocalDB: SignUpLocalDB,
               generator: SignalKeyGenerator, recipientId: String, deviceId: Int)
    : TestUser(generator, recipientId, deviceId) {

    override val store: SignalProtocolStore by lazy {
        SignalStoreCriptext(db)
    }

    init {
        val privateBundle = registrationBundles.privateBundle
        val incompleteAccount = IncompleteAccount(username = recipientId, name = recipientId,
                password = "12345", recoveryEmail ="support@criptext.com")
        val persistedUser = incompleteAccount.complete(privateBundle, "jwt")
        signUpLocalDB.saveNewUserData(persistedUser, privateBundle)
    }

}