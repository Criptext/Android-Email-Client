package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.devices.DeviceItem
import java.text.FieldPosition

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

    sealed class ListDevices : SettingsResult() {
        class Success(val devices: List<DeviceItem>): ListDevices()
        class Failure: ListDevices()
    }

    sealed class RemoveDevice : SettingsResult() {
        class Success(val deviceId: Int, val position: Int): RemoveDevice()
        class Failure: RemoveDevice()
    }

}