package com.criptext.mail.email_preview

import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.scenes.mailbox.data.EmailThread
import java.util.*

/**
 * Created by gabriel on 6/30/18.
 */

data class EmailPreview(val subject: String, val topText: String, val bodyPreview: String,
                        val senderName: String, val deliveryStatus: DeliveryTypes,
                        val unread: Boolean, val count: Int, val timestamp: Date,
                        val latestEmailUnsentDate: Date?,
                        val emailId: Long, val threadId: String, var isSelected: Boolean,
                        val isStarred: Boolean, val hasFiles: Boolean) {

    companion object {
        private fun getSenderNameFromEmailThread(e: EmailThread): String {
            val name = e.latestEmail.from.name
            return if (name.isEmpty()) "Empty" else name
        }

        fun fromEmailThread(e: EmailThread): EmailPreview {
            return EmailPreview(subject = e.subject, bodyPreview = e.preview,
                    topText = e.headerPreview, senderName = getSenderNameFromEmailThread(e),
                    emailId = e.id, deliveryStatus = e.status, unread = e.unread,
                    count = e.totalEmails, timestamp = e.timestamp, threadId = e.threadId,
                    isSelected = false, isStarred = e.isStarred, hasFiles = e.hasFiles,
                    latestEmailUnsentDate = e.latestEmail.email.unsentDate)
        }
    }
}