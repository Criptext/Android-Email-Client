package com.email.scenes.mailbox.data

import com.email.db.models.Label
import com.email.SecureEmail
import com.email.db.DeliveryTypes
import com.email.db.models.FullEmail
import com.email.utils.EmailThreadValidator
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(val latestEmail: FullEmail,
                  val labelsOfMail :List<Label>,
                  val totalEmails: Int) {

    val unread :Boolean
        get() = latestEmail.email.unread
    val threadId = latestEmail.email.threadId
    val timestamp: Date
        get() = latestEmail.email.date
    var isSelected = false
    val headerPreview: String = getContactsInvolved(latestEmail)
    val id: Long
        get() = latestEmail.email.id
    val subject: String
        get() = latestEmail.email.subject
    val preview: String
        get() = latestEmail.email.preview

    val status: DeliveryTypes
        get() = latestEmail.email.delivered

    private fun getContactsInvolved(email: FullEmail): String{

        if(EmailThreadValidator.isLabelInList(email.labels,SecureEmail.LABEL_SENT) && totalEmails > 1){
            return email.to.joinToString { it.name } + ", " + email.from.name +
                    if(email.cc.isEmpty()) "" else email.cc.joinToString { it.name }
        }
        else if(EmailThreadValidator.isLabelInList(email.labels,SecureEmail.LABEL_SENT) && totalEmails == 1){
            return email.to.joinToString { it.name } +
                    if(email.cc.isEmpty()) "" else email.cc.joinToString { it.name }
        }
        else{
            return email.from.name
        }

    }
}