package com.criptext.mail.scenes.settings.data

sealed class SettingsRequest{
    class GetCustomLabels: SettingsRequest()
    class Logout: SettingsRequest()
    data class RemoveDevice(val deviceId: Int, val position: Int, val password: String): SettingsRequest()
    class GetUserSettings: SettingsRequest()
    class ResetPassword: SettingsRequest()
    data class CreateCustomLabel(val labelName: String): SettingsRequest()
    data class ChangeVisibilityLabel(val labelId: Long, val isVisible: Boolean): SettingsRequest()
    data class Set2FA(val twoFA: Boolean): SettingsRequest()
    class SyncBegin: SettingsRequest()
}