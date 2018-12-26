package com.criptext.mail.scenes.composer.data

import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.mailtemplates.FWMailTemplate
import com.criptext.mail.utils.mailtemplates.REMailTemplate
import com.criptext.mail.utils.mailtemplates.SupportMailTemplate

/**
 * Created by sebas on 3/27/18.
 */

sealed class ComposerType {
    class Empty : ComposerType() {
        override fun equals(other: Any?): Boolean = other is Empty
    }
    data class Draft(val draftId: Long, val currentLabel: Label,
                     val threadPreview: EmailPreview): ComposerType()
    data class Reply(val originalId: Long, val currentLabel: Label,
                     val threadPreview: EmailPreview, val template: REMailTemplate): ComposerType()
    data class ReplyAll(val originalId: Long, val currentLabel: Label,
                        val threadPreview: EmailPreview, val template: REMailTemplate): ComposerType()
    data class Forward(val originalId: Long, val currentLabel: Label,
                       val threadPreview: EmailPreview, val template: FWMailTemplate): ComposerType()
    data class Support(val template: SupportMailTemplate): ComposerType()
    data class MailTo(val to: String): ComposerType()
    data class Send(val files: List<Pair<String, Long>>): ComposerType()
}