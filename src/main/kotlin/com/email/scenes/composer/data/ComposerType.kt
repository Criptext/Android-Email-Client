package com.email.scenes.composer.data

import com.email.db.models.Label
import com.email.email_preview.EmailPreview

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
                     val threadPreview: EmailPreview): ComposerType()
    data class ReplyAll(val originalId: Long, val currentLabel: Label,
                        val threadPreview: EmailPreview): ComposerType()
    data class Forward(val originalId: Long, val currentLabel: Label,
                       val threadPreview: EmailPreview): ComposerType()
}