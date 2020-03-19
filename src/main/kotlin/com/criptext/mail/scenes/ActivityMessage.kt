package com.criptext.mail.scenes

import android.graphics.Bitmap
import android.net.Uri
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.utils.UIMessage
import com.google.api.services.drive.Drive
import java.io.File

/**
 * Created by gabriel on 4/3/18.
 */

sealed class ActivityMessage {
    data class SendMail(val emailId: Long,
                        val threadId: String?,
                        val composerInputData: ComposerInputData,
                        val attachments: List<ComposerAttachment>, val fileKey: String?,
                        val senderAddress: String?,
                        val senderAccount: ActiveAccount): ActivityMessage()
    data class SendMailFromExtraAccount(val emailId: Long,
                        val threadId: String?,
                        val composerInputData: ComposerInputData,
                        val attachments: List<ComposerAttachment>, val fileKey: String?): ActivityMessage()
    data class AddAttachments(val filesMetadata: List<Pair<String, Long>>, val isShare: Boolean): ActivityMessage()
    data class AddUrls(val urls: List<String>, val isShare: Boolean): ActivityMessage()
    data class ProfilePictureFile(val filesMetadata: Pair<String, Long>): ActivityMessage()
    data class UpdateUnreadStatusThread(val threadId: String, val unread: Boolean): ActivityMessage()
    data class UpdateLabelsThread(val threadId: String, val selectedLabelIds: List<Long>): ActivityMessage()
    data class UpdateThreadPreview(val threadPreview: EmailPreview): ActivityMessage()
    data class LogoutAccount(val oldAccountEmail: String, val newAccount: ActiveAccount): ActivityMessage()
    class UpdateMailBox: ActivityMessage()
    data class MoveThread(val threadId: String?): ActivityMessage()
    data class DraftSaved(val preview: EmailPreview?): ActivityMessage()
    data class ShowUIMessage(val message: UIMessage): ActivityMessage()
    data class ActivatePin(val isSuccess: Boolean): ActivityMessage()
    class SyncMailbox: ActivityMessage()
    data class GoogleDriveSignIn(val driveService: Drive?): ActivityMessage()
    class ComesFromMailbox: ActivityMessage()
    class LoadDataAtStart: ActivityMessage()
    data class SaveFileToLocalStorage(val uri: Uri): ActivityMessage()
    data class NonValidatedDomainFound(val customDomain: CustomDomain): ActivityMessage()
    data class DomainRegistered(val customDomain: CustomDomain): ActivityMessage()
}