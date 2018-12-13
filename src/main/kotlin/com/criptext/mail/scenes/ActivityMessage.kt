package com.criptext.mail.scenes

import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.utils.UIMessage

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
    class UpdateMailBox: ActivityMessage()
    data class MoveThread(val threadId: String?): ActivityMessage()
    class DraftSaved: ActivityMessage()
    data class ShowUIMessage(val message: UIMessage): ActivityMessage()
    data class ActivatePin(val isSuccess: Boolean): ActivityMessage()
    class SyncMailbox: ActivityMessage()
}