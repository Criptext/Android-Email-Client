package com.criptext.mail.scenes.signup.customize.holder

sealed class CustomizeLayoutState {
    data class AccountCreated(val name: String, val email: String): CustomizeLayoutState()
    data class ProfilePicture(val name: String): CustomizeLayoutState()
    class DarkTheme: CustomizeLayoutState()
    data class Contacts(val hasAllowedContacts: Boolean): CustomizeLayoutState()
    data class VerifyRecoveryEmail(val recoveryEmail: String): CustomizeLayoutState()
    class CloudBackup: CustomizeLayoutState()
}