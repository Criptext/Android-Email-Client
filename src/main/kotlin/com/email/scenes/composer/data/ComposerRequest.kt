package com.email.scenes.composer.data

/**
 * Created by gabriel on 2/26/18.
 */

sealed class ComposerRequest {
    class GetAllContacts : ComposerRequest()
    class SaveEmailAsDraft(val threadId: String?, val emailId: Long?,
                           val composerInputData: ComposerInputData,
                           val onlySave: Boolean): ComposerRequest()
    class DeleteDraft(val emailId: Long): ComposerRequest()
}