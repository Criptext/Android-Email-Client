package com.email.scenes.mailbox.data

import android.text.BoringLayout
import com.email.DB.models.Email
import com.email.DB.models.Label
import com.email.SecureEmail
import com.email.utils.EmailThreadValidator
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(val email : Email, val labelsOfMail :ArrayList<Label>) {

    val unread :Boolean
        get() = email.unread
    val threadId = email.threadid
    val timestamp: Date
        get() = email.date
    var isSelected = false
    val headerPreview: String = if(EmailThreadValidator.isLabelInList(labelsOfMail,SecureEmail.LABEL_SENT)
            || EmailThreadValidator.isLabelInList(labelsOfMail, SecureEmail.LABEL_DRAFT))
        email.preview else email.preview
    val id: Int
        get() = email.id
    val subject: String
        get() = email.subject
    val preview: String
        get() = email.preview

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