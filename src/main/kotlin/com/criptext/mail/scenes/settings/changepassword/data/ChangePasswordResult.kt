package com.criptext.mail.scenes.settings.changepassword.data

import com.criptext.mail.utils.UIMessage

sealed class ChangePasswordResult{

    sealed class ChangePassword : ChangePasswordResult() {
        class Success: ChangePassword()
        data class Failure(val message: UIMessage,
                           val exception: Exception?): ChangePassword()
    }

}