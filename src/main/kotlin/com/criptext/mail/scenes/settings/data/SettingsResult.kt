package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.utils.UIMessage

sealed class SettingsResult{

    sealed class CreateCustomLabel: SettingsResult() {
        data class Success(val label: Label): CreateCustomLabel()
        class Failure: CreateCustomLabel()
    }

    sealed class Logout: SettingsResult() {
        class Success: Logout()
        class Failure: Logout()
    }


    sealed class ChangeContactName : SettingsResult() {
        data class Success(val fullName: String): ChangeContactName()
        class Failure: ChangeContactName()
    }

    sealed class GetCustomLabels : SettingsResult() {
        data class Success(val labels: List<Label>): GetCustomLabels()
        class Failure: GetCustomLabels()
    }

    sealed class ChangeVisibilityLabel : SettingsResult() {
        class Success: ChangeVisibilityLabel()
        class Failure: ChangeVisibilityLabel()
    }

    sealed class GetUserSettings : SettingsResult() {
        data class Success(val userSettings: UserSettingsData): GetUserSettings()
        data class Failure(val message: UIMessage): GetUserSettings()
        data class Unauthorized(val message: UIMessage): GetUserSettings()
    }

    sealed class RemoveDevice : SettingsResult() {
        class Success(val deviceId: Int, val position: Int): RemoveDevice()
        class Failure: RemoveDevice()
    }

    sealed class CheckPassword : SettingsResult() {
        class Success: CheckPassword()
        data class Failure(val message: UIMessage,
                           val exception: Exception?): CheckPassword()
    }

}