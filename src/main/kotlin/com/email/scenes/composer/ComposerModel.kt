package com.email.scenes.composer

import com.email.IHostActivity
import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.data.ComposerTypes
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel() {
    var fullEmail: FullEmail? = null
    var composerType: ComposerTypes? = null
    var emailDetailActivity: IHostActivity? = null

    constructor(fullEmail: FullEmail?, composerType: ComposerTypes?):this() {
        this.fullEmail = fullEmail
        this.composerType = composerType
    }

    val to = LinkedList<Contact>()
    val cc = LinkedList<Contact>()
    val bcc = LinkedList<Contact>()
    val attachments = LinkedHashMap<String, ComposerAttachment>()

    var firstTime = true
    var defaultRecipients: List<Contact> = emptyList()
    var subject = ""
    var body = ""
}