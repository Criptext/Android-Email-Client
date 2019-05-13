package com.criptext.mail.scenes.linking.data

import com.criptext.mail.db.models.ActiveAccount

sealed class LinkingRequest{
    data class CheckForKeyBundle(val incomingAccount: ActiveAccount, val deviceId: Int): LinkingRequest()
}