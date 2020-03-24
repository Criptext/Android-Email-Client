package com.criptext.mail.signal

import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.signup.IncompleteAccount
import com.criptext.mail.scenes.signup.data.StoreAccountTransaction
import org.whispersystems.libsignal.state.SignalProtocolStore

/**
 * Created by gabriel on 3/17/18.
 */

class InDBUser(private val db: AppDatabase, storage: KeyValueStorage, signUpDao: SignUpDao,
               generator: SignalKeyGenerator, recipientId: String, deviceId: Int)
    : DummyUser(generator, recipientId, deviceId) {

    constructor (db: AppDatabase, storage: KeyValueStorage, generator: SignalKeyGenerator,
                 recipientId: String, deviceId: Int) : this(db, storage, db.signUpDao(),
            generator, recipientId, deviceId)

    override val store: SignalProtocolStore by lazy {
        SignalStoreCriptext(db)
    }

    init {
        val privateBundle = registrationBundles.privateBundle
        val incompleteAccount = IncompleteAccount(username = recipientId, name = recipientId,
                password = "12345", recoveryEmail ="support@criptext.com", deviceId = deviceId)
        val persistedUser = incompleteAccount.complete(privateBundle, "__MOCKED_JWT__",
                "__MOCKED_REFRESH__")

        val storeAccountTransaction = StoreAccountTransaction(signUpDao, storage, db.accountDao(), db.aliasDao(), db.customDomainDao())
        storeAccountTransaction.run(persistedUser, privateBundle)

    }

}