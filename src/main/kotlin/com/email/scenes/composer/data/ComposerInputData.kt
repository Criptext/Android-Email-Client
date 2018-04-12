package com.email.scenes.composer.data

import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.composer.ComposerModel

/**
 * Created by gabriel on 2/26/18.
 */

data class ComposerInputData(val to: List<Contact>, val cc: List<Contact>,
                             val bcc: List<Contact>, val subject: String, val body: String) {

    val hasAtLeastOneRecipient: Boolean
        get () = to.isNotEmpty() || cc.isNotEmpty() || bcc.isNotEmpty()

    companion object {
        fun fromModel(model: ComposerModel): ComposerInputData = ComposerInputData(to = model.to,
                cc = model.cc, bcc = model.bcc, subject = model.subject, body = model.body)
    }
}