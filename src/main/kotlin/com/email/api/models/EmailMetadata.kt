package com.email.api.models

import com.email.db.models.Contact
import com.email.signal.SignalEncryptedData
import org.json.JSONObject

data class EmailMetadata(
        val to: String,
        val cc: String,
        val bcc: String,
        val from: String,
        val fromRecipientId: String,
        val fromContact: Contact,
        val messageId: String,
        val date: String,
        val messageType: SignalEncryptedData.Type,
        val threadId: String,
        val subject: String) {


    companion object {
        fun fromJSON(metadataJsonString: String): EmailMetadata {
            val emailData = JSONObject(metadataJsonString)
            val from = emailData.getString("from")
            // TODO make this more robust
            val fromEmail = from.substring(from.indexOf("<") + 1, from.indexOf(">"))
            val fromName = from.substring(0, from.lastIndexOf("<") - 1)
            val fromRecipientId = fromEmail.substring(0, fromEmail.indexOf("@"))
            val fromContact = Contact(id = 0, email = fromEmail, name = fromName)
            val messageType = emailData.getInt("messageType")
            val isPreKeyMessage = messageType == SignalEncryptedData.Type.preKey.toInt()
            return EmailMetadata(
                    from = from,
                    fromRecipientId = fromRecipientId,
                    fromContact = fromContact,
                    to = emailData.getString("to"),
                    cc = emailData.getString("cc"),
                    bcc = emailData.getString("bcc"),
                    messageId = emailData.getString("messageId"),
                    date = emailData.getString("date"),
                    threadId = emailData.getString("threadId"),
                    subject = emailData.getString("subject"),
                    messageType = if (isPreKeyMessage) SignalEncryptedData.Type.preKey
                                  else SignalEncryptedData.Type.normal
            )

        }
    }
}