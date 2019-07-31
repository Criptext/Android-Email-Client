package com.criptext.mail.scenes.composer.data

import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.composer.ComposerModel

/**
 * Created by gabriel on 2/26/18.
 */

data class ComposerInputData(val to: List<Contact>, val cc: List<Contact>,
                             val bcc: List<Contact>, val subject: String,
                             val body: String, val attachments: ArrayList<ComposerAttachment>?,
                             val fileKey: String?) {

    val hasAtLeastOneRecipient: Boolean
        get () = to.isNotEmpty() || cc.isNotEmpty() || bcc.isNotEmpty()

    companion object {
        fun fromModel(model: ComposerModel): ComposerInputData = ComposerInputData(to = model.to,
                cc = model.cc, bcc = model.bcc, subject = model.subject, body = model.body,
                attachments = model.attachments, fileKey = model.fileKey)
    }
}