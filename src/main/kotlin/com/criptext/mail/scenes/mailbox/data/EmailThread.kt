package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.EmailThreadValidator
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(val latestEmail: FullEmail,
                  val participants: List<Contact>,
                  val currentLabel: String,
                  val totalEmails: Int,
                  val hasFiles: Boolean,
                  val allFilesAreInline: Boolean,
                  val headerData: List<HeaderData>) {

    val unread :Boolean
        get() = latestEmail.email.unread
    val threadId = latestEmail.email.threadId
    val timestamp: Date
        get() = latestEmail.email.date
    var isSelected = false
    val headerPreview: String = if(participants.size != 1) participants.joinToString { it.name.substringBefore(" ") }
                                    else  participants.joinToString { it.name }
    val id: Long
        get() = latestEmail.email.id
    val metadataKey: Long
        get() = latestEmail.email.metadataKey
    val subject: String
        get() = latestEmail.email.subject
    val preview: String
        get() = latestEmail.email.preview

    val status: DeliveryTypes
        get() = latestEmail.email.delivered

    val isStarred: Boolean
        get() = EmailThreadValidator.isLabelInList(latestEmail.labels, Label.LABEL_STARRED)

    data class HeaderData(val name: String, val isMe: Boolean, val isDraft: Boolean, val isUnread: Boolean)
}