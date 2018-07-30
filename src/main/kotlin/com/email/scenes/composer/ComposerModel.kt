package com.email.scenes.composer

import com.email.db.models.Contact
import com.email.db.models.Label
import com.email.email_preview.EmailPreview
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.data.ComposerType
import com.email.validation.FormInputState
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel(val type: ComposerType) {

    val isReplyOrDraft: Boolean = type is ComposerType.Reply
            || type is ComposerType.ReplyAll || type is ComposerType.Draft

    var attachments: ArrayList<ComposerAttachment> = ArrayList()

    val to = LinkedList<Contact>()
    val cc = LinkedList<Contact>()
    val bcc = LinkedList<Contact>()

    var firstTime = true
    var initialized = type is ComposerType.Empty
    var subject = ""
    var body = ""

    var passwordText: String = ""
    var confirmPasswordText: String = ""
    var passwordState: FormInputState = FormInputState.Unknown()

    var passwordForNonCriptextUsers: String? = null

    var fileKey: String? = null
}