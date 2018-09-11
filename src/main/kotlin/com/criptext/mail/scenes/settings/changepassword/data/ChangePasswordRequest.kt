package com.criptext.mail.scenes.settings.changepassword.data

sealed class ChangePasswordRequest{
    data class ChangePassword(val oldPassword: String, val newPassword: String): ChangePasswordRequest()
}