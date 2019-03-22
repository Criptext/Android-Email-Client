package com.criptext.mail.scenes.signup.data

import com.criptext.mail.scenes.signup.IncompleteAccount

/**
 * Created by sebas on 2/26/18.
 */

sealed class SignUpRequest{
    data class RegisterUser(val account: IncompleteAccount,
                            val recipientId: String,
                            val isMultiple: Boolean
                       ): SignUpRequest()
    data class CheckUserAvailability(val username: String): SignUpRequest()
}
