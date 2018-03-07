package com.email.scenes.signup

/**
 * Created by sebas on 3/7/18.
 */

data class IncompleteAccount(
        val username: String,
        val name: String,
        val password: String,
        val recoveryEmail: String?
        )