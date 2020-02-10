package com.criptext.mail.scenes.mailbox.feed.data

import com.criptext.mail.db.models.CRFile
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.FeedItem

/**
 * Created by danieltigse on 2/7/18.
 */

class ActivityFeedItem(feedItem: FeedItem,
                       val email: Email,
                       contact: Contact,
                       file: CRFile?){

    val id = feedItem.id
    val type = feedItem.feedType
    val date = feedItem.date
    val seen = feedItem.seen
    val contactName = contact.name
    val fileName = file?.name ?: ""
    val emailSubject = email.subject
    val threadId = email.threadId

}