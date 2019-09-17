package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.ResultHeaders
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.Contact
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.UIMessage
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */
sealed class ComposerResult {

    sealed class GetAllContacts : ComposerResult() {
        data class Success(val contacts: List<Contact>): GetAllContacts()
        data class Failure(val message: UIMessage): GetAllContacts()
    }

    sealed class GetAllFromAddresses : ComposerResult() {
        data class Success(val accounts: List<Account>): GetAllFromAddresses()
        data class Failure(val message: UIMessage): GetAllFromAddresses()
    }

    sealed class LoadInitialData : ComposerResult() {
        data class Success(val initialData: ComposerInputData) : LoadInitialData()
        data class Failure(val message: UIMessage) : LoadInitialData()
    }

    sealed class SaveEmail : ComposerResult() {
        data class Success(val emailId: Long, val threadId: String,
                           val composerInputData: ComposerInputData,
                           val onlySave: Boolean, val attachments: List<ComposerAttachment>,
                           val fileKey: String?,
                           val preview: EmailPreview?) : SaveEmail()

        class TooManyRecipients: SaveEmail()
        class Failure: SaveEmail()
    }

    sealed class UploadFile : ComposerResult() {
        data class Success(val filepath: String, val filesSize: Long, val uuid: String): UploadFile()
        data class Register(val filepath: String, val filetoken: String, val uuid: String): UploadFile()
        data class Progress(val filepath: String, val percentage: Int, val uuid: String): UploadFile()
        data class MaxFilesExceeds(val filepath: String): UploadFile()
        data class PayloadTooLarge(val filepath: String, val headers: ResultHeaders): UploadFile()
        data class Failure(val filepath: String, val message: UIMessage): UploadFile()
        data class Unauthorized(val message: UIMessage): UploadFile()
        class Forbidden: UploadFile()
        class EnterpriseSuspended(): UploadFile()
    }

    sealed class CheckDomain : ComposerResult() {
        data class Success(val contactDomainCheck: List<ContactDomainCheckData>): CheckDomain()
        data class Failure(val message: UIMessage): CheckDomain()
    }
}