package com.criptext.mail.scenes.settings.profile.data

import com.criptext.mail.db.models.ActiveAccount

data class ProfileUserData(var name: String, val email: String,
                           var isEmailConfirmed: Boolean, var recoveryEmail: String,
                           var replyToEmail: String?, val isLastDeviceWith2FA: Boolean) {

    constructor() : this(name = "",
            email = "",
            replyToEmail = "",
            recoveryEmail = "",
            isLastDeviceWith2FA = false,
            isEmailConfirmed = false)

    companion object {
        fun getDefaultData(activeAccount: ActiveAccount): ProfileUserData{
            return ProfileUserData(
                    name = activeAccount.name,
                    email = activeAccount.userEmail,
                    replyToEmail = "",
                    recoveryEmail = "",
                    isLastDeviceWith2FA = false,
                    isEmailConfirmed = false
            )
        }

        fun getDefaultData(): ProfileUserData{
            return ProfileUserData(
                    name = "",
                    email = "",
                    replyToEmail = "",
                    recoveryEmail = "",
                    isLastDeviceWith2FA = false,
                    isEmailConfirmed = false
            )
        }
    }
}