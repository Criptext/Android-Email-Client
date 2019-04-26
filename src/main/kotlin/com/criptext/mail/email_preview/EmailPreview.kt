package com.criptext.mail.email_preview

import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.DateAndTimeUtils
import org.json.JSONObject
import java.util.*

/**
 * Created by gabriel on 6/30/18.
 */

data class EmailPreview(val subject: String, val topText: String, val bodyPreview: String,
                        val sender: Contact, val deliveryStatus: DeliveryTypes,
                        val unread: Boolean, val count: Int, val timestamp: Date,
                        val latestEmailUnsentDate: Date?,
                        val emailId: Long, val metadataKey: Long, val threadId: String,
                        var isSelected: Boolean, var isStarred: Boolean, val hasFiles: Boolean,
                        val allFilesAreInline: Boolean, val headerData: List<EmailThread.HeaderData>) {

    companion object {
        private fun getSenderNameFromEmailThread(e: EmailThread): String {
            val name = e.latestEmail.from.name
            return if (name.isEmpty()) "Empty" else name
        }

        fun fromEmailThread(e: EmailThread): EmailPreview {
            return EmailPreview(subject = e.subject, bodyPreview = e.preview,
                    topText = e.headerPreview, sender = e.latestEmail.from,
                    emailId = e.id, deliveryStatus = e.status, unread = e.unread,
                    count = e.totalEmails, timestamp = e.timestamp, threadId = e.threadId,
                    isSelected = false, isStarred = e.isStarred, hasFiles = e.hasFiles,
                    latestEmailUnsentDate = e.latestEmail.email.unsentDate, metadataKey = e.metadataKey,
                    allFilesAreInline = e.allFilesAreInline, headerData = e.headerData)
        }

        fun emailPreviewToJSON(e: EmailPreview): String {
            val json = JSONObject()
            json.put("subject", e.subject)
            json.put("topText", e.topText)
            json.put("bodyPreview", e.bodyPreview)
            json.put("sender", Contact.toJSON(e.sender))
            json.put("emailId", e.emailId)
            json.put("deliveryStatus", DeliveryTypes.getTrueOrdinal(e.deliveryStatus))
            json.put("unread", e.unread)
            json.put("count", e.count)
            json.put("timestamp", DateAndTimeUtils.printDateWithServerFormat(e.timestamp))
            json.put("threadId", e.threadId)
            json.put("isSelected", e.isSelected)
            json.put("isStarred", e.isStarred)
            json.put("hasFiles", e.hasFiles)
            json.put("allFilesAreInline", e.allFilesAreInline)
            if(e.latestEmailUnsentDate != null)
                json.put("latestEmailUnsentDate", DateAndTimeUtils.printDateWithServerFormat(e.latestEmailUnsentDate))
            json.put("metadataKey", e.metadataKey)

            return json.toString()
        }

        fun emailPreviewFromJSON(e: String): EmailPreview {
            val json = JSONObject(e)
            return EmailPreview(subject = json.getString("subject"), bodyPreview = json.getString("topText"),
                    topText = json.getString("bodyPreview"), sender = Contact.fromJSON(json.getString("sender").toString()),
                    emailId = json.getLong("emailId"), deliveryStatus = DeliveryTypes.fromInt(json.getInt("deliveryStatus")),
                    unread = json.getBoolean("unread"), count = json.getInt("count"),
                    timestamp = DateAndTimeUtils.getDateFromString(
                            json.getString("timestamp"), null), threadId = json.getString("threadId"),
                    isSelected = json.getBoolean("isSelected"), isStarred = json.getBoolean("isStarred"),
                    hasFiles = json.getBoolean("hasFiles"),
                    latestEmailUnsentDate = if(json.has("latestEmailUnsentDate"))
                    DateAndTimeUtils.getDateFromString(
                            json.getString("latestEmailUnsentDate"), null) else null, metadataKey = json.getLong("metadataKey"),
                    allFilesAreInline = json.getBoolean("allFilesAreInline"),
                    headerData = listOf())
        }
    }
}