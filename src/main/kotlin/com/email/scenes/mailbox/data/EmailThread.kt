package com.email.scenes.mailbox.data

import com.email.db.models.Email
import com.email.db.models.Label
import com.email.SecureEmail
import com.email.utils.EmailThreadValidator
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(val latestEmail : Email,
                  val labelsOfMail :ArrayList<Label>) {

    val unread :Boolean
        get() = latestEmail.unread
    val threadId = latestEmail.threadid
    val timestamp: Date
        get() = latestEmail.date
    var isSelected = false
    val headerPreview: String = if(EmailThreadValidator.isLabelInList(labelsOfMail,SecureEmail.LABEL_SENT)
            || EmailThreadValidator.isLabelInList(labelsOfMail, SecureEmail.LABEL_DRAFT))
        latestEmail.preview else latestEmail.preview
    val id: Int
        get() = latestEmail.id!!
    val subject: String
        get() = latestEmail.subject
    val preview: String
        get() = latestEmail.preview

    val replyType: ReplyTypes
        get() {
            val length = subject.length
            if (length < 4)
                return ReplyTypes.none
            val title = subject.substring(0, Math.min(length, 5)).toLowerCase()
            return if (title.startsWith("re: "))
                ReplyTypes.reply
            else if (title.startsWith("fwd: ") || title.startsWith("fw: "))
                ReplyTypes.forward
            else ReplyTypes.none
        }

}