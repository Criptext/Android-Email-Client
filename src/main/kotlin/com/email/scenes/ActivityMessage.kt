package com.email.scenes

import com.email.email_preview.EmailPreview
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.data.ComposerInputData

/**
 * Created by gabriel on 4/3/18.
 */

sealed class ActivityMessage {
    data class SendMail(val emailId: Long,
                        val threadId: String?,
                        val composerInputData: ComposerInputData,
                        val attachments: List<ComposerAttachment>, val fileKey: String?): ActivityMessage()
    data class AddAttachments(val filesMetadata: List<Pair<String, Long>>): ActivityMessage()
    data class UpdateUnreadStatusThread(val threadId: String, val unread: Boolean): ActivityMessage()
    data class UpdateLabelsThread(val threadId: String, val selectedLabelIds: List<Long>): ActivityMessage()
    data class UpdateThreadPreview(val threadPreview: EmailPreview): ActivityMessage()
    data class MoveThread(val threadId: String?): ActivityMessage()
}