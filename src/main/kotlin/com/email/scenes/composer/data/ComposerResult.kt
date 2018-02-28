package com.email.scenes.composer.data

import com.email.DB.models.Contact

/**
 * Created by gabriel on 2/26/18.
 */
sealed class ComposerResult {
    sealed class SendMail: ComposerResult() {
        class Success: SendMail()
        class Failure(val message: String): SendMail()
    }

    sealed class SuggestContacts: ComposerResult() {
        class Success(val suggestions: List<Contact>): SuggestContacts()
        class Failure(val message: String): SuggestContacts()
    }
}