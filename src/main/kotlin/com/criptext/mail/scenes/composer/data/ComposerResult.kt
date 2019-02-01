package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.ResultHeaders
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */
sealed class ComposerResult {

    sealed class GetAllContacts : ComposerResult() {
        data class Success(val contacts: List<Contact>): GetAllContacts()
        data class Failure(val message: String): GetAllContacts()
    }

    sealed class LoadInitialData : ComposerResult() {
        data class Success(val initialData: ComposerInputData) : LoadInitialData()
        data class Failure(val message: UIMessage) : LoadInitialData()
    }

    sealed class SaveEmail : ComposerResult() {
        data class Success(val emailId: Long, val threadId: String,
                           val composerInputData: ComposerInputData,
                           val onlySave: Boolean, val attachments: List<ComposerAttachment>,
                           val fileKey: String?) : SaveEmail()

        class TooManyRecipients: SaveEmail()
        class Failure: SaveEmail()
    }

    sealed class UploadFile : ComposerResult() {
        data class Success(val filepath: String, val filesSize: Long): UploadFile()
        data class Register(val filepath: String, val filetoken: String): UploadFile()
        data class Progress(val filepath: String, val percentage: Int): UploadFile()
        data class MaxFilesExceeds(val filepath: String): UploadFile()
        data class PayloadTooLarge(val filepath: String, val headers: ResultHeaders): UploadFile()
        data class Failure(val filepath: String, val message: UIMessage): UploadFile()
        data class Unauthorized(val message: UIMessage): UploadFile()
        class Forbidden: UploadFile()
    }

    sealed class DeleteDraft : ComposerResult() {
        class Success: DeleteDraft()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): DeleteDraft()
    }
}