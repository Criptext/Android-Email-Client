package com.criptext.mail.scenes.composer

import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.validation.FormInputState
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel(val type: ComposerType): SceneModel {

    val isReplyOrDraft: Boolean = type is ComposerType.Reply
            || type is ComposerType.ReplyAll || type is ComposerType.Draft

    val isSupport: Boolean = type is ComposerType.Support

    var attachments: ArrayList<ComposerAttachment> = ArrayList()

    val filesExceedingMaxEmailSize = mutableListOf<String>()
    val filesExceedingMaxFileSize = mutableListOf<Pair<String, String>>()

    var isUploadingAttachments = false

    val to = LinkedList<Contact>()
    val cc = LinkedList<Contact>()
    val bcc = LinkedList<Contact>()

    var firstTime = true
    var initialized = type is ComposerType.Empty
    var subject = ""
    var body = ""

    var passwordText: String = ""

    var passwordForNonCriptextUsers: String? = null

    var fileKey: String? = null

    var filesSize: Long = 0L
}