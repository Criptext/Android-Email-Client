package com.email.scenes.composer.data

import com.email.api.JSONData
import com.email.signal.SignalEncryptedData
import org.json.JSONObject

/**
 * Created by gabriel on 3/21/18.
 */

class PostEmailBody(val threadId: String?, val subject: String,
                    val criptextEmails: List<CriptextEmail>, val guestEmail: GuestEmail?,
                    val attachments: List<CriptextAttachment>): JSONData {

    enum class RecipientTypes { to, cc, bcc, peer }

    override fun toJSON(): JSONObject {
        val criptextEmailsArray = criptextEmails.toJSONArray()

        val json = JSONObject()
        json.put("threadId", threadId)
        json.put("subject", subject)
        json.put("criptextEmails", criptextEmailsArray)
        if(attachments.isNotEmpty()){
            val attachmentsArray = attachments.toJSONArray()
            json.put("files", attachmentsArray)
        }

        return json
    }

    data class CriptextAttachment(val token: String): JSONData {
        override fun toJSON(): JSONObject {
            val json = JSONObject()
            json.put("token", token)
            return json
        }
    }

    data class CriptextEmail(val recipientId: String, val deviceId: Int,
                             val messageType: SignalEncryptedData.Type,
                             val type: RecipientTypes, val body: String): JSONData {

        override fun toJSON(): JSONObject {
            val json = JSONObject()
            json.put("recipientId", recipientId)
            json.put("deviceId", deviceId)
            json.put("type", type.toString())
            json.put("body", body)
            json.put("messageType", messageType.toInt())
            return json
        }
    }

    data class GuestEmail(val to: List<String>, val cc: List<String>, val bcc: List<String>,
                          val body: String, val session: String)

}