package com.email.scenes.signup.mocks

import com.email.db.SignUpLocalDB

/**
 * Created by sebas on 2/27/18.
 */

class MockedSignUpLocalDB : SignUpLocalDB {
    override fun login(): Boolean {
        return true
    }
}