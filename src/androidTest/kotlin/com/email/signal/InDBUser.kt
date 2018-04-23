package com.email.signal

import com.email.db.AppDatabase
import com.email.db.dao.SignUpDao
import com.email.db.KeyValueStorage
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.scenes.signup.IncompleteAccount
import com.email.scenes.signup.data.StoreAccountTransaction
import org.whispersystems.libsignal.state.SignalProtocolStore

/**
 * Created by gabriel on 3/17/18.
 */

class InDBUser(private val db: AppDatabase, storage: KeyValueStorage, signUpDao: SignUpDao,
               generator: SignalKeyGenerator, recipientId: String, deviceId: Int)
    : TestUser(generator, recipientId, deviceId) {

    constructor (db: AppDatabase, storage: KeyValueStorage, generator: SignalKeyGenerator,
                 recipientId: String, deviceId: Int) : this(db, storage, db.signUpDao(),
            generator, recipientId, deviceId)

    override val store: SignalProtocolStore by lazy {
        SignalStoreCriptext(db)
    }

    init {
        val privateBundle = registrationBundles.privateBundle
        val incompleteAccount = IncompleteAccount(username = recipientId, name = recipientId,
                password = "12345", recoveryEmail ="support@criptext.com")
        val persistedUser = incompleteAccount.complete(privateBundle, "__MOCKED_JWT__")

        val storeAccountTransaction = StoreAccountTransaction(signUpDao, storage)
        storeAccountTransaction.run(persistedUser, privateBundle)

    }

}