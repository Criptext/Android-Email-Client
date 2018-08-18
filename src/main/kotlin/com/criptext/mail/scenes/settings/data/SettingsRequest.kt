package com.criptext.mail.scenes.settings.data

sealed class SettingsRequest{
    data class ChangeContactName(val fullName: String, val recipientId: String) : SettingsRequest()
    class GetCustomLabels: SettingsRequest()
    class Logout: SettingsRequest()
    data class RemoveDevice(val deviceId: Int, val position: Int): SettingsRequest()
    data class CheckPassword(val oldPassword: String, val newPassword: String): SettingsRequest()
    class GetUserSettings: SettingsRequest()
    data class CreateCustomLabel(val labelName: String): SettingsRequest()
    data class ChangeVisibilityLabel(val labelId: Long, val isVisible: Boolean): SettingsRequest()
}