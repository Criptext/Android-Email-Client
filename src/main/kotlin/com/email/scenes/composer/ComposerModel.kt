package com.email.scenes.composer

import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.composer.data.ComposerTypes
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel {
    var fullEmail: FullEmail? = null
    var composerType: ComposerTypes? = null

    constructor(fullEmail: FullEmail?, composerType: ComposerTypes?) {
        this.fullEmail = fullEmail
        this.composerType = composerType
    }

    val to = LinkedList<Contact>()
    val cc = LinkedList<Contact>()
    val bcc = LinkedList<Contact>()

    var firstTime = true
    var defaultRecipients: List<Contact> = emptyList()
    var subject = ""
    var body = ""
}