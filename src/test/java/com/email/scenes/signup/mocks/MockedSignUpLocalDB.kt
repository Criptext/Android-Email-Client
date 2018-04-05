package com.email.scenes.signup.mocks

import com.email.db.SignUpLocalDB
import com.email.db.models.Account
import com.email.signal.SignalKeyGenerator

/**
 * Created by sebas on 2/27/18.
 */

class MockedSignUpLocalDB : SignUpLocalDB {
    var savedUser: Account? = null
    private set

    override fun saveNewUserData(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle) {
        savedUser = account
    }

    override fun seedLabels() {
    }

}