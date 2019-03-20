package com.criptext.mail.scenes.settings.data

sealed class SettingsRequest{
    class Logout: SettingsRequest()
    class GetUserSettings: SettingsRequest()
    class ResetPassword: SettingsRequest()
    class SyncBegin: SettingsRequest()
}