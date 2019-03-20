package com.criptext.mail.scenes.settings.profile.data

data class ProfileUserData(var name: String, val email: String,
                           var isEmailConfirmed: Boolean, var recoveryEmail: String,
                           var replyToEmail: String?, val isLastDeviceWith2FA: Boolean)