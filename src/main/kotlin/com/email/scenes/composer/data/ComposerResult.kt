package com.email.scenes.composer.data

import com.email.db.models.Contact
import com.email.db.models.File
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */
sealed class ComposerResult {

    sealed class GetAllContacts : ComposerResult() {
        data class Success(val contacts: List<Contact>): GetAllContacts()
        data class Failure(val message: String): GetAllContacts()
    }

    sealed class SaveEmail : ComposerResult() {
        data class Success(val emailId: Long, val threadId: String,
                           val composerInputData: ComposerInputData,
                           val onlySave: Boolean, val attachments: List<ComposerAttachment>) : SaveEmail()
        class Failure: SaveEmail()
    }

    sealed class UploadFile : ComposerResult() {
        data class Success(val filepath: String): UploadFile()
        data class Register(val filepath: String, val size: Long, val filetoken: String): UploadFile()
        data class Progress(val filepath: String, val percentage: Int): UploadFile()
        data class Failure(val filepath: String, val message: UIMessage): UploadFile()
    }

    sealed class DeleteDraft : ComposerResult() {
        class Success: DeleteDraft()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): DeleteDraft()
    }
}