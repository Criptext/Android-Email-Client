package com.email.scenes.composer.data

import com.email.api.HttpClient
import com.email.db.models.File

/**
 * Created by gabriel on 2/26/18.
 */

sealed class ComposerRequest {
    class GetAllContacts : ComposerRequest()
    class SaveEmailAsDraft(val threadId: String?, val emailId: Long?,
                           val composerInputData: ComposerInputData,
                           val onlySave: Boolean, val attachments: List<ComposerAttachment>): ComposerRequest()
    class DeleteDraft(val emailId: Long): ComposerRequest()
    class UploadAttachment(val filepath: String, val httpClient: HttpClient,
                           val authToken: String): ComposerRequest()
}