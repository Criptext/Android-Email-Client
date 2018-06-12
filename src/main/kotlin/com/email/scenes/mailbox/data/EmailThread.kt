package com.email.scenes.mailbox.data

import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.db.models.Label
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(val latestEmail: FullEmail,
                  val participants: List<Contact>,
                  val currentLabel: MailFolders,
                  val totalEmails: Int) {

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
}