package com.criptext.mail.scenes.signup.holders

sealed class SignUpLayoutState {
    data class Name(val name: String): SignUpLayoutState()
    data class EmailHandle(val emailHandle: String): SignUpLayoutState()
    data class Password(val password: String): SignUpLayoutState()
    data class ConfirmPassword(val confirmPassword: String): SignUpLayoutState()
    data class RecoveryEmail(val recoveryEmail: String): SignUpLayoutState()
    class TermsAndConditions: SignUpLayoutState()
}