package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.JSONData
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.file.FileUtils
import org.json.JSONArray
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
        json.put("guestEmail", guestEmail?.toJSON())
        if(attachments.isNotEmpty()){
            val attachmentsArray = attachments.toJSONArray()
            json.put("files", attachmentsArray)
        }

        return json
    }

    data class CriptextAttachment(val token: String, val name: String, val size: Long): JSONData {
        override fun toJSON(): JSONObject {
            val json = JSONObject()
            json.put("token", token)
            json.put("name", name)
            json.put("size", size)
            json.put("mimeType", FileUtils.getMimeType(name))
            return json
        }
    }

    data class CriptextEmail(val recipientId: String, val deviceId: Int,
                             val messageType: SignalEncryptedData.Type,
                             val type: RecipientTypes, val body: String, val fileKey: String?): JSONData {

        override fun toJSON(): JSONObject {
            val json = JSONObject()
            json.put("recipientId", recipientId)
            json.put("deviceId", deviceId)
            json.put("type", type.toString())
            json.put("body", body)
            json.put("messageType", messageType.toInt())
            json.put("fileKey", fileKey)
            return json
        }
    }

    data class GuestEmail(val to: List<String>, val cc: List<String>, val bcc: List<String>,
                          val body: String, val salt: String? , val iv: String?,
                          val session: String?):JSONData{
        override fun toJSON(): JSONObject {
            val json = JSONObject()
            json.put("to", JSONArray(to))
            json.put("cc", JSONArray(cc))
            json.put("bcc", JSONArray(bcc))
            json.put("body", body)
            if(session != null && iv != null && salt != null) {
                val saltBytes = Encoding.stringToByteArray(salt)
                val ivBytes = Encoding.stringToByteArray(iv)
                val sessionBytes =  Encoding.stringToByteArray(session)
                val newEncodedSession = Encoding.byteArrayToString(
                        saltBytes + ivBytes + sessionBytes)
                json.put("session", newEncodedSession)
            }else
                json.put("session", null)
            return json
        }
    }

}