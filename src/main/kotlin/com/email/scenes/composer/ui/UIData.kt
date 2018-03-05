package com.email.scenes.composer.ui

import com.email.db.models.Contact
import com.email.scenes.composer.ComposerModel

/**
 * Created by gabriel on 2/26/18.
 */

data class UIData(val to: List<Contact>, val cc: List<Contact>, val bcc: List<Contact>,
                  val subject: String, val body: String) {
    companion object {
        fun fromModel(model: ComposerModel): UIData = UIData(to = model.to, cc = model.cc,
                bcc = model.bcc, subject = model.subject, body = model.body)
    }
}