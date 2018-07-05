package com.email.scenes.settings.data

sealed class SettingsRequest{
    data class ChangeContactName(val fullName: String, val recipientId: String) : SettingsRequest()
    class GetCustomLabels: SettingsRequest()
}