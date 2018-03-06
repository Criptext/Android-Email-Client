package com.email.scenes.signup.data

import com.email.api.PreKeyBundleShareData
import com.email.db.models.User

/**
 * Created by sebas on 2/26/18.
 */

sealed class SignUpRequest{
    class RegisterUser(val user: User,
                       val password: String,
                       val recoveryEmail: String?,
                       val recipientId: String
                       ): SignUpRequest()
}
