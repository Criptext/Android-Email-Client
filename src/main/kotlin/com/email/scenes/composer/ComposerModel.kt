package com.email.scenes.composer

import com.email.DB.models.Contact
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel {
    val to = LinkedList<Contact>()
    val cc = LinkedList<Contact>()
    val bcc = LinkedList<Contact>()

    var subject = ""
    var body = ""
}