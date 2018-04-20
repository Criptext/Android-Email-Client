package com.email.scenes.composer.data

/**
 * Created by gabriel on 2/26/18.
 */

sealed class ComposerRequest {
    class GetAllContacts : ComposerRequest()
    class SaveEmailAsDraftAndSend(val composerInputData: ComposerInputData): ComposerRequest()
    class SaveEmailAsDraft(val composerInputData: ComposerInputData): ComposerRequest()
}