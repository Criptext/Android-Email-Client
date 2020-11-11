package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.utils.UIMessage

sealed class SettingsResult{
    sealed class ResetPassword : SettingsResult() {
        class Success: ResetPassword()
        data class Failure(val message: UIMessage): ResetPassword()
    }

    sealed class UpdateSignature: SettingsResult() {
        class Success: UpdateSignature()
        data class Failure(val message: UIMessage): UpdateSignature()
    }

    sealed class CheckCustomDomain: SettingsResult() {
        data class Success(val customDomain: CustomDomain): CheckCustomDomain()
        data class Failure(val message: UIMessage): CheckCustomDomain()
    }
}