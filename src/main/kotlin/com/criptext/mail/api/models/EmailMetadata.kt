package com.criptext.mail.api.models

import com.criptext.mail.api.toList
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.CRFile
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FileKey
import com.criptext.mail.signal.SignalEncryptedData
import org.json.JSONObject

/**
 * data class for email metadata. This is received as params of a "new email" event (1).
 *
 */
data class EmailMetadata(
        val to: List<String>,
        val cc: List<String>,
        val bcc: List<String>,
        val from: String,
        val senderRecipientId: String,
        val senderDeviceId: Int?,
        val fromContact: Contact,
        val messageId: String,
        val metadataKey: Long,
        val date: String,
        val messageType: SignalEncryptedData.Type?,
        val threadId: String,
        val subject: String,
        val files: List<CRFile>,
        val fileKey: String?,
        val secure: Boolean) {

    fun extractDBColumns(): DBColumns =
            DBColumns(to = to, cc = cc, bcc = bcc, messageId = messageId, threadId = threadId,
                    metadataKey = metadataKey, subject = subject, date = date, unsentDate = null,
                    fromContact = fromContact, unread = true, status = DeliveryTypes.NONE, secure = true)

    companion object {
        fun fromJSON(metadataJsonString: String): EmailMetadata {
            val  emailData = JSONObject(metadataJsonString)
            val from = emailData.getString("from")
            // TODO make this more robust
            val fromEmail = if(from.contains("<") && from.contains(">"))
                    from.substring(from.lastIndexOf("<") + 1, from.lastIndexOf(">")) else from
            val fromName = if(from.contains("<") && from.contains(">"))
                    from.substring(0, from.lastIndexOf("<") - 1) else from
            val fromRecipientId = fromEmail.substring(0, fromEmail.indexOf("@"))
            val fromContact = Contact(id = 0, email = fromEmail, name = fromName)
            val messageType = emailData.optInt("messageType")
            val senderDeviceId = emailData.optInt("senderDeviceId")
            val files = CRFile.listFromJSON(metadataJsonString)
            val fileKey = FileKey.fromJSON(metadataJsonString)
            return EmailMetadata(
                    from = from,
                    senderRecipientId = fromRecipientId,
                    senderDeviceId = if (senderDeviceId != 0) senderDeviceId else null,
                    fromContact = fromContact,
                    to = getToArray(emailData),
                    cc = getCCArray(emailData),
                    bcc = getBCCArray(emailData),
                    messageId = emailData.getString("messageId"),
                    metadataKey = emailData.getLong("metadataKey"),
                    date = emailData.getString("date"),
                    threadId = emailData.getString("threadId"),
                    subject = emailData.getString("subject"),
                    messageType = SignalEncryptedData.Type.fromInt(messageType),
                    files = files,
                    fileKey = fileKey.key,
                    secure = true
            )

        }

        private fun getToArray(emailData: JSONObject): List<String>{
            return when {
                emailData.has("toArray") -> emailData.getJSONArray("toArray").toList()
                emailData.has("to") -> {
                    if(emailData.getString("to") == "") emptyList()
                    else emailData.getString("to").split(",")
                }
                else -> emptyList()
            }
        }

        private fun getCCArray(emailData: JSONObject): List<String>{
            return when {
                emailData.has("ccArray") -> emailData.getJSONArray("ccArray").toList()
                emailData.has("cc") -> {
                    if(emailData.getString("cc") == "") emptyList()
                    else emailData.getString("cc").split(",")
                }
                else -> emptyList()
            }
        }

        private fun getBCCArray(emailData: JSONObject): List<String> {
            return when {
                emailData.has("bccArray") -> emailData.getJSONArray("bccArray").toList()
                emailData.has("bcc") -> {
                    if(emailData.getString("bcc") == "") emptyList()
                    else emailData.getString("bcc").split(",")
                }
                else -> emptyList()
            }
        }
    }

    /**
     * EmailMetadata class has a couple of fields that are not persisted to the database.
     * The DBColumns class is a subset of EmailMetadata, containing only the data that you will
     * definitely need to persist.
     */
    data class DBColumns(
        val to: List<String>,
        val cc: List<String>,
        val bcc: List<String>,
        val messageId: String,
        val metadataKey: Long,
        val date: String,
        val unsentDate: String?,
        val threadId: String,
        val fromContact: Contact,
        val subject: String,
        val unread: Boolean,
        val status: DeliveryTypes,
        val secure: Boolean)
}