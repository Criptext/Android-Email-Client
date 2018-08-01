package com.criptext.mail.services.data

sealed class MessagingServiceResult {

    sealed class RefreshTokenOnServer: MessagingServiceResult() {
        data class Success(val refreshedToken: String): RefreshTokenOnServer()
        class Failure: RefreshTokenOnServer()
    }
}