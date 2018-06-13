package com.email.scenes.signup.data

import com.email.scenes.signup.IncompleteAccount

/**
 * Created by sebas on 2/26/18.
 */

sealed class SignUpRequest{
    data class RegisterUser(val account: IncompleteAccount,
                       val recipientId: String
                       ): SignUpRequest()
    data class CheckUserAvailability(val username: String): SignUpRequest()
}
