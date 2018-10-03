package com.criptext.mail.scenes.linking.data

sealed class LinkingRequest{
    data class CheckForKeyBundle(val deviceId: Int): LinkingRequest()
}