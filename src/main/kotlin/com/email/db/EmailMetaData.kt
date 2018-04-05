package com.email.db

import org.json.JSONObject

class EmailMetaData(stringMetadata: String) {
    val to: String
    val cc: String
    val bcc: String
    val from: String
    val fromRecipientId: String
    val fromName: String
    val bodyKey: String
    val date: String
    val threadId: String
    val subject: String

    init {
        val emailData = JSONObject(stringMetadata)

        from = emailData.getString("from")
        fromRecipientId = from.substring(from.indexOf("<") + 1, from.indexOf(">"))
        fromName = from.substring(0, from.lastIndexOf("<") - 1)
        to = emailData.getString("to")
        cc = emailData.getString("cc")
        bcc = emailData.getString("bcc")
        bodyKey = emailData.getString("bodyKey")
        date = emailData.getString("date")
        threadId = emailData.getString("threadId")
        subject = emailData.getString("subject")
    }
}