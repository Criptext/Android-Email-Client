package com.email.db

import org.json.JSONObject

class EmailMetaData(stringMetadata: String) {
    val to: String
    val cc: String
    val bcc: String
    val from: String
    val bodyKey: String
    val date: String
    val threadId: String
    val subject: String

    init {
        val emailData = JSONObject(stringMetadata)

        from = emailData.getString("from")
        to = emailData.getString("to")
        cc = emailData.getString("cc")
        bcc = emailData.getString("bcc")
        bodyKey = emailData.getString("bodyKey")
        date = emailData.getString("date")
        threadId = emailData.getString("threadId")
        subject = emailData.getString("subject")
    }
}