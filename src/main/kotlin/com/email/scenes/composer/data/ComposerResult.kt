package com.email.scenes.composer.data

import com.email.db.models.Contact
import com.email.db.models.Email

/**
 * Created by gabriel on 2/26/18.
 */
sealed class ComposerResult {

    sealed class GetAllContacts : ComposerResult() {
        data class Success(val contacts: List<Contact>): GetAllContacts()
        data class Failure(val message: String): GetAllContacts()
    }

    sealed class SaveEmail : ComposerResult() {
        data class Success(val emailId: Long, val onlySave: Boolean) : SaveEmail()
        class Failure: SaveEmail()
    }

}