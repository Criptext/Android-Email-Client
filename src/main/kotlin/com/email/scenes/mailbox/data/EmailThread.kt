package com.email.scenes.mailbox.data

import com.email.SecureEmail
import com.email.db.DeliveryTypes
import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.utils.EmailThreadValidator
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(val latestEmail: FullEmail,
                  val participants: List<Contact>,
                  val currentLabel: String,
                  val totalEmails: Int,
                  val hasFiles: Boolean) {

    val unread :Boolean
        get() = latestEmail.email.unread
    val threadId = latestEmail.email.threadId
    val timestamp: Date
        get() = latestEmail.email.date
    var isSelected = false
    val headerPreview: String = participants.joinToString { it.name }
    val id: Long
        get() = latestEmail.email.id
    val subject: String
        get() = latestEmail.email.subject
    val preview: String
        get() = latestEmail.email.preview

    val status: DeliveryTypes
        get() = latestEmail.email.delivered

    val isStarred: Boolean
        get() = EmailThreadValidator.isLabelInList(latestEmail.labels, SecureEmail.LABEL_STARRED)
}