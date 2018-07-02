package com.email.scenes.composer

import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.data.ComposerType
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel(val type: ComposerType) {
    val threadId = when (type) {
        is ComposerType.Reply -> type.threadId
        is ComposerType.ReplyAll -> type.threadId
        else -> null
    }

    var attachments: ArrayList<ComposerAttachment> = ArrayList()

    val to = LinkedList<Contact>()
    val cc = LinkedList<Contact>()
    val bcc = LinkedList<Contact>()

    var firstTime = true
    var initialized = false
    var defaultRecipients: List<Contact> = emptyList()
    var subject = ""
    var body = ""
}