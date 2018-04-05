package com.email.scenes.composer.data

import com.email.db.models.Contact
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */
sealed class ComposerResult {

    sealed class SuggestContacts: ComposerResult() {
        data class Success(val suggestions: List<Contact>): SuggestContacts()
        data class Failure(val message: String): SuggestContacts()
    }
}