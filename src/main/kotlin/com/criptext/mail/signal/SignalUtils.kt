package com.criptext.mail.signal

import com.criptext.mail.api.toList
import com.criptext.mail.db.models.Contact
import org.json.JSONObject
import org.whispersystems.libsignal.SignalProtocolAddress

object SignalUtils{
    const val externalRecipientId: String = "bob"

    fun getSignalAddressFromJSON(
            jsonString : String
    ): List<SignalProtocolAddress> {
        val jsonObject = JSONObject(jsonString)
        val domain = jsonObject.getString("domain")
        val recipientId = if(domain == Contact.mainDomain)
            jsonObject.getString("name")
        else
            jsonObject.getString("name").plus("@$domain")
        val devices = jsonObject.getJSONArray("devices").toList<Int>()
        return devices.map { SignalProtocolAddress(recipientId, it) }
    }
}