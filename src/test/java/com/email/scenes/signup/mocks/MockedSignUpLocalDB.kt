package com.email.scenes.signup.mocks

import com.email.db.SignUpLocalDB
import com.email.db.models.Account
import com.email.db.models.signal.CRSignedPreKey

/**
 * Created by sebas on 2/27/18.
 */

class MockedSignUpLocalDB : SignUpLocalDB {
    var savedUser: Account? = null
    private set

    override fun deletePrekeys() {
    }

    override fun storePrekeys(prekeys: Map<Int, String>) {
    }

    override fun saveAccount(account: Account) {
        savedUser = account
    }

    override fun storeRawSignedPrekey(crSignedPreKey: CRSignedPreKey) {
    }

}