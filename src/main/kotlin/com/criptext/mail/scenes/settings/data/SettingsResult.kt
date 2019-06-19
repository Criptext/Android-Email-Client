package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.UIMessage

sealed class SettingsResult{

    sealed class Logout: SettingsResult() {
        class Success: Logout()
        class Failure: Logout()
    }

    sealed class GetUserSettings : SettingsResult() {
        data class Success(val userSettings: UserSettingsData): GetUserSettings()
        data class Failure(val message: UIMessage): GetUserSettings()
        data class Unauthorized(val message: UIMessage): GetUserSettings()
        class Forbidden: GetUserSettings()
        class EnterpriseSuspended: GetUserSettings()
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