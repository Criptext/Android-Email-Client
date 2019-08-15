package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.UIMessage

sealed class SettingsResult{

    sealed class Logout: SettingsResult() {
        class Success: Logout()
        data class Failure(val message: UIMessage): Logout()
    }

    sealed class ResetPassword : SettingsResult() {
        class Success: ResetPassword()
        data class Failure(val message: UIMessage): ResetPassword()
    }

    sealed class SyncBegin: SettingsResult() {
        class Success: SyncBegin()
        data class NoDevicesAvailable(val message: UIMessage): SyncBegin()
        data class Failure(val message: UIMessage): SyncBegin()
    }
}