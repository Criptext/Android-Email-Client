package com.criptext.mail.signal

import com.criptext.mail.api.toList
import org.json.JSONObject
import org.whispersystems.libsignal.SignalProtocolAddress

object SignalUtils{
    const val externalRecipientId: String = "bob"

    fun getSignalAddressFromJSON(
            jsonString : String
    ): List<SignalProtocolAddress> {
        val jsonObject = JSONObject(jsonString)
        val recipientId = jsonObject.getString("name")
        val devices = jsonObject.getJSONArray("devices").toList<Int>()
        return devices.map { SignalProtocolAddress(recipientId, it) }
    }
}