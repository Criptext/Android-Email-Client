package com.email.views.mailbox.data

import com.email.DB.models.Email
import com.email.DB.models.Label
import com.email.SecureEmail
import com.email.utils.ListUtils

/**
 * Created by sebas on 1/24/18.
 */

class EmailThread(private val latestMail : Email, private val labelsOfMail :ArrayList<Label>, val count:Int, hasEmailAttachments:Boolean ) {

    val headerPreview: String = if(ListUtils.isLabelInList(labelsOfMail,SecureEmail.LABEL_SENT)
            || ListUtils.isLabelInList(labelsOfMail, SecureEmail.LABEL_DRAFT))
        latestMail.preview as String else latestMail.preview as String // cambiar esto.

    val isread: Boolean by lazy { latestMail.unread!!.equals(1) }

}