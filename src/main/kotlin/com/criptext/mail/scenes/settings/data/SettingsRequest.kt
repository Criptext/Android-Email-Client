package com.criptext.mail.scenes.settings.data

sealed class SettingsRequest{
    data class ChangeContactName(val fullName: String, val recipientId: String) : SettingsRequest()
    class GetCustomLabels: SettingsRequest()
    data class CreateCustomLabel(val labelName: String): SettingsRequest()
    data class ChangeVisibilityLabel(val labelId: Long, val isVisible: Boolean): SettingsRequest()
}